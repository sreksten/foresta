package com.threeamigos.foresta.libgdx;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Lo {@link SpriteBatch} di sempre, con le coordinate delle texture in precisione alta nel fragment shader. Quello predefinito di libGDX dichiara
 * <code>precision mediump float</code>, e su molte GPU dei telefoni e dei tablet (le Mali, per esempio) il <code>mediump</code> e' un virgola mobile a 16
 * bit: su un atlante alto oltre mille pixel una coordinata sbaglia di uno o piu' texel, e al bordo di un'immagine si legge un pezzo del vicino. Gli atlanti
 * di questo gioco separano ogni immagine con una riga rossa di un pixel, quindi l'errore si vede come un bordo rosso attorno alle immagini (sul desktop,
 * dove il <code>mediump</code> e' a 32 bit, non succede). Con <code>GL_FRAGMENT_PRECISION_HIGH</code> si usa <code>highp</code>; dove la GPU non lo ha,
 * resta <code>mediump</code> come prima.
 * <p>
 * Lo shader e' quello predefinito di libGDX (vertici, colore moltiplicato per il texel) salvo la precisione.
 */
public final class SpriteBatchDiPrecisioneAltaGdx extends SpriteBatch {

	private static final String VERTICI = "attribute vec4 a_position;\n"
			+ "attribute vec4 a_color;\n"
			+ "attribute vec2 a_texCoord0;\n"
			+ "uniform mat4 u_projTrans;\n"
			+ "varying vec4 v_color;\n"
			+ "varying vec2 v_texCoords;\n"
			+ "\n"
			+ "void main()\n"
			+ "{\n"
			+ "   v_color = a_color;\n"
			+ "   v_color.w = v_color.w * (255.0/254.0);\n"
			+ "   v_texCoords = a_texCoord0;\n"
			+ "   gl_Position =  u_projTrans * a_position;\n"
			+ "}\n";

	private static final String FRAMMENTI = "#ifdef GL_ES\n"
			+ "#define LOWP lowp\n"
			+ "#ifdef GL_FRAGMENT_PRECISION_HIGH\n"
			+ "precision highp float;\n"
			+ "#else\n"
			+ "precision mediump float;\n"
			+ "#endif\n"
			+ "#else\n"
			+ "#define LOWP \n"
			+ "#endif\n"
			+ "varying LOWP vec4 v_color;\n"
			+ "varying vec2 v_texCoords;\n"
			+ "uniform sampler2D u_texture;\n"
			+ "void main()\n"
			+ "{\n"
			+ "  gl_FragColor = v_color * texture2D(u_texture, v_texCoords);\n"
			+ "}";

	private final ShaderProgram shader;

	public SpriteBatchDiPrecisioneAltaGdx() {
		this(creaLoShader());
	}

	private SpriteBatchDiPrecisioneAltaGdx(ShaderProgram shader) {
		super(1000, shader);
		this.shader = shader;
	}

	private static ShaderProgram creaLoShader() {
		ShaderProgram shader = new ShaderProgram(VERTICI, FRAMMENTI);
		if (!shader.isCompiled()) {
			throw new IllegalStateException("Lo shader del batch non compila: " + shader.getLog());
		}
		return shader;
	}

	/**
	 * Libera anche lo shader, che lo {@link SpriteBatch} non considera suo (gliel'abbiamo dato noi)
	 */
	@Override
	public void dispose() {
		super.dispose();
		shader.dispose();
	}
}
