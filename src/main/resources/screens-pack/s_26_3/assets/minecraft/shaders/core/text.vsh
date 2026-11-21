#version 330
#extension GL_ARB_separate_shader_objects : require

#if !defined(IS_GUI) && !defined(IS_SEE_THROUGH)
#include <minecraft:fog.glsl>
#include <minecraft:sample_lightmap.glsl>
#endif

#include <minecraft:dynamictransforms.glsl>
#include <minecraft:projection.glsl>

layout(location = 0) in vec3 Position;
layout(location = 1) in vec4 Color;
layout(location = 2) in vec2 UV0;
#if !defined(IS_GUI) && !defined(IS_SEE_THROUGH)
layout(location = 3) in ivec2 UV2;
#endif

#if !defined(IS_GUI) && !defined(IS_SEE_THROUGH)
uniform sampler2D Sampler2;
layout(location = 0) out float sphericalVertexDistance;
layout(location = 1) out float cylindricalVertexDistance;
#endif

layout(location = 2) out vec4 vertexColor;
layout(location = 3) out vec2 texCoord0;


// ---- SCore Screens (cursor and image tiles of the PINNED menus) ----
bool score_world() {
    return ProjMat[2][3] != 0.0;
}

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
}
