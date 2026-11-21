#!/usr/bin/env python3
"""Generate the SCore Screens resource pack (src/main/resources/screens-pack/).

The pack gives the PINNED screens their mouse-like cursor and their image tiles. It holds the vanilla text vertex
shaders of each client generation with two extra branches:

* cursor: a text vertex whose colour is exactly (254, 254, 254) is mirrored through the view axis
  (view.xy = -view.xy) and pulled to depth 0.1, so the glyph of a world point one block ahead moves with the head
  like a mouse pointer and is drawn over everything;
* image tile: a 128x128 texture (a map) whose row 0 starts with the signature 0xB5A3 is placed on the camera at depth
  0.12, at the panel position written in that row (see ScreenGeometry.mapHeader).

GUI text (orthographic projection) is never touched.

Usage: generate.py <minecraft versions dir>   (client jars: <dir>/<version>/<version>.jar)
"""
import io
import json
import os
import shutil
import sys
import zipfile

from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
OUT = os.path.join(HERE, "..", "..", "src", "main", "resources", "screens-pack")

# overlay directory -> (client jar to read, min format, max format, file layout)
FAMILIES = [
    # base pack (1.21.4 - 1.21.5, formats 46 - 62)
    ("", "1.21.4", 46, 62, "split"),
    ("s_1_21_6", "1.21.6", 63, 64, "split"),
    ("s_1_21_9", "1.21.9", 65, 83, "split"),
    ("s_26_1", "26.1.2", 84, 87, "split"),
    ("s_26_2", "26.2", 88, 96, "unified"),
    ("s_26_3", "26.3", 97, 99, "unified"),
]
MAX_FORMAT = 99
CORE = "assets/minecraft/shaders/core/"

FUNCS = """
// ---- SCore Screens (cursor and image tiles of the PINNED menus) ----
bool score_world() {
    return ProjMat[2][3] != 0.0;
}
"""
FUNCS_MAP = """
int score_word(int start) {
    int v = 0;
    for (int i = 0; i < 16; i++) {
        v = v * 2 + (texelFetch(Sampler0, ivec2(start + i, 0), 0).r > 0.5 ? 1 : 0);
    }
    return v;
}
"""


def cursor_block(fog):
    return """
    // SCore Screens: cursor (colour key 254, 254, 254)
    if (score_world() && all(equal(ivec3(round(Color.rgb * 255.0)), ivec3(254)))) {
        vec4 scoreView = ModelViewMat * vec4(Position, 1.0);
        scoreView.xy = -scoreView.xy;
        if (scoreView.z < -0.0001) scoreView.xyz *= 0.1 / -scoreView.z;
        gl_Position = ProjMat * scoreView;
        vertexColor = vec4(1.0);
%s    }
""" % fog


def map_block(fog):
    return """
    // SCore Screens: image tile (map with the 0xB5A3 header in row 0)
    if (score_world() && textureSize(Sampler0, 0) == ivec2(128, 128) && score_word(0) == 46499) {
        vec2 scoreCentre = vec2(float(score_word(16)), float(score_word(32))) / 4096.0 - 8.0;
        vec2 scoreHalf = vec2(float(score_word(48)), float(score_word(64))) / 4096.0;
        vec2 scoreCorner = vec2(2.0 * UV0.x - 1.0, 1.0 - 2.0 * UV0.y);
        gl_Position = ProjMat * vec4((scoreCentre + scoreCorner * scoreHalf) * 0.12, -0.12, 1.0);
        texCoord0 = vec2(UV0.x, (1.0 + UV0.y * 127.0) / 128.0);
        vertexColor = vec4(1.0);
%s    }
""" % fog


def fog_reset(src, guard=None):
    lines = ""
    for name in ("vertexDistance", "sphericalVertexDistance", "cylindricalVertexDistance"):
        if ("out float %s;" % name) in src:
            lines += "        %s = 0.0;\n" % name
    if lines and guard:
        lines = "#if %s\n%s#endif\n" % (guard, lines)
    return lines


def patch(src, with_map, guard_cursor=None, guard_map=None, guard_fog=None):
    """insert the helper functions before main() and the branches at the end of main()"""
    if "void main() {" not in src or not src.rstrip().endswith("}"):
        raise SystemExit("unexpected shader layout")
    funcs = FUNCS
    if with_map:
        sampler = "" if "uniform sampler2D Sampler0;" in src else "uniform sampler2D Sampler0;\n"
        block = sampler + FUNCS_MAP
        funcs += ("#if %s\n%s#endif\n" % (guard_map, block)) if guard_map else block
    head, tail = src.split("void main() {", 1)
    body = tail.rstrip()[:-1]
    cur = cursor_block(fog_reset(src, guard_fog))
    if guard_cursor:
        cur = "#if %s\n%s#endif\n" % (guard_cursor, cur)
    extra = cur
    if with_map:
        mp = map_block(fog_reset(src))
        if guard_map:
            mp = "#if %s\n%s#endif\n" % (guard_map, mp)
        extra += mp
    return head + funcs + "\nvoid main() {" + body + extra + "}\n"


def read(jar, name):
    with zipfile.ZipFile(jar) as z:
        return z.read(name).decode("utf-8")


ARROW = [
    "1...............",
    "11..............",
    "121.............",
    "1221............",
    "12221...........",
    "122221..........",
    "1222221.........",
    "12222221........",
    "122222221.......",
    "1222222221......",
    "12222211111.....",
    "1221221.........",
    "121.1221........",
    "11..1221........",
    "1....1221.......",
    ".....111........",
]


def pointer_png():
    """16x16 arrow cursor, tip at the top-left, then turned 180 degrees (the shader mirror turns it back).
    After the turn the tip is the bottom-right pixel, so the glyph keeps its full 16 px width."""
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y, row in enumerate(ARROW):
        for x, c in enumerate(row):
            if c == "1":
                img.putpixel((x, y), (16, 16, 20, 255))
            elif c == "2":
                img.putpixel((x, y), (255, 255, 255, 255))
    img = img.rotate(180)
    out = io.BytesIO()
    img.save(out, "PNG", optimize=True)
    return out.getvalue()


def main():
    if len(sys.argv) != 2:
        raise SystemExit(__doc__)
    versions = sys.argv[1]
    if os.path.isdir(OUT):
        shutil.rmtree(OUT)
    os.makedirs(OUT)
    entries = []
    for directory, version, lo, hi, layout in FAMILIES:
        jar = os.path.join(versions, version, version + ".jar")
        root = os.path.join(OUT, directory) if directory else OUT
        core = os.path.join(root, CORE)
        os.makedirs(core, exist_ok=True)
        if layout == "split":
            text = read(jar, CORE + "rendertype_text.vsh")
            see = read(jar, CORE + "rendertype_text_see_through.vsh")
            open(os.path.join(core, "rendertype_text.vsh"), "w").write(patch(text, True))
            open(os.path.join(core, "rendertype_text_see_through.vsh"), "w").write(patch(see, False))
        else:
            text = read(jar, CORE + "text.vsh")
            maps = True  # 26.3 too: our panel is depth-tested, so the draw order of maps does not matter
            open(os.path.join(core, "text.vsh"), "w").write(
                patch(text, maps, guard_cursor="!defined(IS_GUI)",
                      guard_map="!defined(IS_GUI) && !defined(IS_SEE_THROUGH)",
                      guard_fog="!defined(IS_SEE_THROUGH)"))
        if directory:
            entries.append({"formats": [lo, hi], "min_format": lo, "max_format": hi, "directory": directory})

    font = os.path.join(OUT, "assets", "minecraft", "font")
    os.makedirs(font)
    json.dump({"providers": [{"type": "bitmap", "file": "score:screens/pointer.png", "ascent": 7, "height": 8,
                              "chars": ["\U0010FFF0"]}]},
              open(os.path.join(font, "default.json"), "w"), indent=2)
    tex = os.path.join(OUT, "assets", "score", "textures", "screens")
    os.makedirs(tex)
    open(os.path.join(tex, "pointer.png"), "wb").write(pointer_png())

    meta = {"pack": {"pack_format": 46, "supported_formats": [46, MAX_FORMAT], "min_format": 46,
                     "max_format": MAX_FORMAT,
                     "description": "SCore Screens: cursor of the PINNED menus (sent to admins only)"},
            "overlays": {"entries": entries}}
    json.dump(meta, open(os.path.join(OUT, "pack.mcmeta"), "w"), indent=2)

    files = []
    for dp, _, fs in os.walk(OUT):
        for f in fs:
            files.append(os.path.relpath(os.path.join(dp, f), OUT).replace(os.sep, "/"))
    files.sort()
    open(os.path.join(OUT, "files.txt"), "w").write("\n".join(f for f in files if f != "files.txt") + "\n")
    print("\n".join(files))


if __name__ == "__main__":
    main()
