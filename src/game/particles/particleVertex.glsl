#version 130

layout(location = 0) in vec2 position;
layout(location = 1) in mat4 modelMatrix;
layout(location = 5) in vec4 textureOffsets;
layout(location = 6) in float blendFactor;
layout(location = 7) in float transparency;

varying vec2 textureCoords1;
varying vec2 textureCoords2;
varying float textureBlendFactor;
varying float textureTransparency;

uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;
uniform vec4 clipPlane;
uniform float numberOfRows;

void main(void) {
	mat4 modelViewMatrix = viewMatrix * modelMatrix;
	gl_ClipDistance[0] = dot(modelMatrix * vec4(position, 0.0, 1.0), clipPlane);
	gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 0.0, 1.0);

	vec2 textureCoords = position + vec2(0.5, 0.5);
	textureCoords.y = 1.0 - textureCoords.y;
	textureCoords /= numberOfRows;

	textureCoords1 = textureCoords + textureOffsets.xy;
	textureCoords2 = textureCoords + textureOffsets.zw;
	textureBlendFactor = blendFactor;
	textureTransparency = transparency;
}
