#ifdef GL_ES
    #define LOWP lowp
    precision mediump float;
#else
    #define LOWP
#endif
varying LOWP vec4 vColor;
varying vec2 vTexCoord;

uniform sampler2D u_texture;
uniform float width;
uniform float height;
uniform float radius;
uniform int mode;
uniform vec3 color;

// Nice website for computing Gaussian coeffs:
//  http://dev.theomader.com/gaussian-kernel-calculator/
// coeef1D:
//    0.000229, 0.005977, 0.060598, 0.241732, 0.382928, 0.241732, 0.060598, 0.005977, 0.000229
// coeff2D:
//    0.000000, 0.000001, 0.000014, 0.000055, 0.000088, 0.000055, 0.000014, 0.000001, 0.000000,
//    0.000001, 0.000036, 0.000362, 0.001445, 0.002289, 0.001445, 0.000362, 0.000036, 0.000001,
//    0.000014, 0.000362, 0.003672, 0.014648, 0.023205, 0.014648, 0.003672, 0.000362, 0.000014,
//    0.000055, 0.001445, 0.014648, 0.058434, 0.092566, 0.058434, 0.014648, 0.001445, 0.000055,
//    0.000088, 0.002289, 0.023205, 0.092566, 0.146634, 0.092566, 0.023205, 0.002289, 0.000088,
//    0.000055, 0.001445, 0.014648, 0.058434, 0.092566, 0.058434, 0.014648, 0.001445, 0.000055,
//    0.000014, 0.000362, 0.003672, 0.014648, 0.023205, 0.014648, 0.003672, 0.000362, 0.000014,
//    0.000001, 0.000036, 0.000362, 0.001445, 0.002289, 0.001445, 0.000362, 0.000036, 0.000001,
//    0.000000, 0.000001, 0.000014, 0.000055, 0.000088, 0.000055, 0.000014, 0.000001, 0.000000

void main() {
	vec4 sum = vec4(0.0);
	vec2 tc = vTexCoord;
	float hblur = radius/width;
	float vblur = radius/height;

    if (mode==1) { // edge detect
        vec4 p = texture2D(u_texture, tc);
        if (p.a < 0.5) {
            gl_FragColor = vec4(color.rgb, 0.0);
        } else {
            p = texture2D(u_texture, vec2(tc.x + 1.0*hblur, tc.y));
            p = min(p, texture2D(u_texture, vec2(tc.x - 1.0*hblur, tc.y)));
            p = min(p, texture2D(u_texture, vec2(tc.x, tc.y + 1.0*vblur)));
            p = min(p, texture2D(u_texture, vec2(tc.x, tc.y - 1.0*vblur)));

            p = min(p, texture2D(u_texture, vec2(tc.x + 1.0*hblur, tc.y + 1.0*vblur)));
            p = min(p, texture2D(u_texture, vec2(tc.x - 1.0*hblur, tc.y + 1.0*vblur)));
            p = min(p, texture2D(u_texture, vec2(tc.x + 1.0*hblur, tc.y - 1.0*vblur)));
            p = min(p, texture2D(u_texture, vec2(tc.x - 1.0*hblur, tc.y - 1.0*vblur)));

            gl_FragColor = vec4(color.rgb, 1.0 - step(0.5, p.a));
        }
    } else if (mode==0) { // blur
	    // 2-D Gaussian filter (we ignore any coeff less than 0.01)
	    sum += texture2D(u_texture, vec2(tc.x - 1.0*hblur, tc.y - 2.0*vblur)) * 0.014648;
	    sum += texture2D(u_texture, vec2(tc.x,             tc.y - 2.0*vblur)) * 0.023205;
	    sum += texture2D(u_texture, vec2(tc.x + 1.0*hblur, tc.y - 2.0*vblur)) * 0.014648;

	    sum += texture2D(u_texture, vec2(tc.x - 2.0*hblur, tc.y - 1.0*vblur)) * 0.014648;
	    sum += texture2D(u_texture, vec2(tc.x - 1.0*hblur, tc.y - 1.0*vblur)) * 0.058434;
	    sum += texture2D(u_texture, vec2(tc.x,             tc.y - 1.0*vblur)) * 0.092566;
	    sum += texture2D(u_texture, vec2(tc.x + 1.0*hblur, tc.y - 1.0*vblur)) * 0.058434;
	    sum += texture2D(u_texture, vec2(tc.x + 2.0*hblur, tc.y - 1.0*vblur)) * 0.014648;

	    sum += texture2D(u_texture, vec2(tc.x - 2.0*hblur, tc.y            )) * 0.023205;
	    sum += texture2D(u_texture, vec2(tc.x - 1.0*hblur, tc.y            )) * 0.092566;
	    sum += texture2D(u_texture, vec2(tc.x,             tc.y            )) * 0.146634;
	    sum += texture2D(u_texture, vec2(tc.x + 1.0*hblur, tc.y            )) * 0.092566;
	    sum += texture2D(u_texture, vec2(tc.x + 2.0*hblur, tc.y            )) * 0.023205;

	    sum += texture2D(u_texture, vec2(tc.x - 2.0*hblur, tc.y + 1.0*vblur)) * 0.014648;
	    sum += texture2D(u_texture, vec2(tc.x - 1.0*hblur, tc.y + 1.0*vblur)) * 0.058434;
	    sum += texture2D(u_texture, vec2(tc.x,             tc.y + 1.0*vblur)) * 0.092566;
	    sum += texture2D(u_texture, vec2(tc.x + 1.0*hblur, tc.y + 1.0*vblur)) * 0.058434;
	    sum += texture2D(u_texture, vec2(tc.x + 2.0*hblur, tc.y + 1.0*vblur)) * 0.014648;

	    sum += texture2D(u_texture, vec2(tc.x - 1.0*hblur, tc.y + 2.0*vblur)) * 0.014648;
	    sum += texture2D(u_texture, vec2(tc.x,             tc.y + 2.0*vblur)) * 0.023205;
	    sum += texture2D(u_texture, vec2(tc.x + 1.0*hblur, tc.y + 2.0*vblur)) * 0.014648;
	    sum *= 4.0;

    	gl_FragColor = vColor * (vec4(sum.rgba) + vec4(texture2D(u_texture, tc)));
    } else { // pass-through
        gl_FragColor = vColor * vec4(texture2D(u_texture, tc));
    }
}