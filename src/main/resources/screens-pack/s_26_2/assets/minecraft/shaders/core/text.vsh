#version 330

#if !defined(IS_GUI) && !defined(IS_SEE_THROUGH)
#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:sample_lightmap.glsl>
#endif

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;
#if !defined(IS_GUI) && !defined(IS_SEE_THROUGH)
in ivec2 UV2;
#endif

#if !defined(IS_GUI) && !defined(IS_SEE_THROUGH)
uniform sampler2D Sampler2;
out float sphericalVertexDistance;
out float cylindricalVertexDistance;
#endif

out vec4 vertexColor;
out vec2 texCoord0;


// ---- SCore Screens (cursor and image tiles of the PINNED menus) ----
bool score_world() {
    return ProjMat[2][3] != 0.0;
}
#if !defined(IS_GUI) && !defined(IS_SEE_THROUGH)
uniform sampler2D Sampler0;

int score_word(int start) {
    int v = 0;
    for (int i = 0; i < 16; i++) {
        v = v * 2 + (texelFetch(Sampler0, ivec2(start + i, 0), 0).r > 0.5 ? 1 : 0);
    }
    return v;
}
#endif

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

#if !defined(IS_GUI) && !defined(IS_SEE_THROUGH)
    sphericalVertexDistance = fog_spherical_distance(Position);
    cylindricalVertexDistance = fog_cylindrical_distance(Position);
    vertexColor = Color * sample_lightmap(Sampler2, UV2);
#else
    vertexColor = Color;
#endif
    texCoord0 = UV0;
#if !defined(IS_GUI)

    // SCore Screens: cursor (colour key 254, 254, 254)
    if (score_world() && all(equal(ivec3(round(Color.rgb * 255.0)), ivec3(254)))) {
        vec4 scoreView = ModelViewMat * vec4(Position, 1.0);
        scoreView.xy = -scoreView.xy;
        if (scoreView.z < -0.0001) scoreView.xyz *= 0.1 / -scoreView.z;
        gl_Position = ProjMat * scoreView;
        vertexColor = vec4(1.0);
#if !defined(IS_SEE_THROUGH)
        sphericalVertexDistance = 0.0;
        cylindricalVertexDistance = 0.0;
#endif
    }
#endif
#if !defined(IS_GUI) && !defined(IS_SEE_THROUGH)

    // SCore Screens: image tile (map with the 0xB5A3 header in row 0)
    if (score_world() && textureSize(Sampler0, 0) == ivec2(128, 128) && score_word(0) == 46499) {
        vec2 scoreCentre = vec2(float(score_word(16)), float(score_word(32))) / 4096.0 - 8.0;
        vec2 scoreHalf = vec2(float(score_word(48)), float(score_word(64))) / 4096.0;
        vec2 scoreCorner = vec2(2.0 * UV0.x - 1.0, 1.0 - 2.0 * UV0.y);
        gl_Position = ProjMat * vec4((scoreCentre + scoreCorner * scoreHalf) * 0.12, -0.12, 1.0);
        texCoord0 = vec2(UV0.x, (1.0 + UV0.y * 127.0) / 128.0);
        vertexColor = vec4(1.0);
        sphericalVertexDistance = 0.0;
        cylindricalVertexDistance = 0.0;
    }
#endif
}
