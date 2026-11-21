#version 150

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;

out vec4 vertexColor;
out vec2 texCoord0;


// ---- SCore Screens (cursor and image tiles of the PINNED menus) ----
bool score_world() {
    return ProjMat[2][3] != 0.0;
}

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    vertexColor = Color;
    texCoord0 = UV0;

    // SCore Screens: cursor (colour key 254, 254, 254)
    if (score_world() && all(equal(ivec3(round(Color.rgb * 255.0)), ivec3(254)))) {
        vec4 scoreView = ModelViewMat * vec4(Position, 1.0);
        scoreView.xy = -scoreView.xy;
        if (scoreView.z < -0.0001) scoreView.xyz *= 0.1 / -scoreView.z;
        gl_Position = ProjMat * scoreView;
        vertexColor = vec4(1.0);
    }
}
