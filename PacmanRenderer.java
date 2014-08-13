package com.pacman.android;

import static android.opengl.GLES20.*;
import static android.opengl.GLUtils.*;
import static android.opengl.Matrix.*;
import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glViewport;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.airhockey.android.R;
import com.pacman.util.*;

import android.content.Context;
import android.opengl.GLSurfaceView.Renderer;

public class PacmanRenderer implements Renderer{

	private static final int POSITION_COMPONENT_COUNT = 4;
	
	private static final int BYTES_PER_FLOAT = 4;
	
	private final FloatBuffer vertexData;
	
	private final Context context;
	
	private int program;
	
//	private static final String U_COLOR = "u_Color";
//	private int uColorLocation;
	
	private static final String A_POSITION = "a_Position";
	private int aPositionLocation;
	
	private static final String A_COLOR = "a_Color";
	private static final int COLOR_COMPONENT_COUNT = 3;
	private static final int STRIDE =
	(POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT) * BYTES_PER_FLOAT;
	
	private int aColorLocation;
	
	private static final String U_MATRIX = "u_Matrix";
	private final float[] projectionMatrix = new float[16];
	private int uMatrixLocation;
	
	private final float[] modelMatrix = new float[16];
	
	public PacmanRenderer(Context context) 
	{
		this.context = context;
		float[] tableVerticesWithTriangles = {
				
				// Order of coordinates: X, Y, Z, W, R, G, B
				// Triangle 1 for underground
				-1.0f, -1.0f, 0f, 1f, 0.7f, 0.7f, 0.7f,
				1.0f, -1.0f, 0f, 1f, 0.7f, 0.7f, 0.7f,
				1.0f, 1.0f, 0f, 1f, 0.7f, 0.7f, 0.7f,
				
				// Triangle 2 for underground
				-1.0f, -1.0f, 0f, 1f, 0.7f, 0.7f, 0.7f,
				-1.0f, 1.0f, 0f, 1f, 0.7f, 0.7f, 0.7f,
				1.0f, 1.0f, 0f, 1f, 0.7f, 0.7f, 0.7f,
				
				
				//middle Prison
				// Triangle 1 for Prison top
				-0.15f, 0.15f, 0f, 1f, 0f, 1f, 0f,
				0.15f, 0.15f, 0f, 1f, 0f, 1f, 0f,
				0.15f, 0.15f, 0.1f, 1f, 0f, 1f, 0f,
				
				// Triangle 2 for Prison top
				-0.15f, 0.15f, 0f, 1f, 0f, 1f, 0f,
				-0.15f, 0.15f, 0.1f, 1f, 0f, 1f, 0f,
				0.15f, 0.15f, 0.1f, 1f, 0f, 1f, 0f,
				
				//middle Prison
				// Triangle 1 for Prison bottom
				-0.15f, -0.15f, 0f, 1f, 0f, 1f, 0f,
				0.15f, -0.15f, 0f, 1f, 0f, 1f, 0f,
				0.15f, -0.15f, 0.1f, 1f, 0f, 1f, 0f,
				
				// Triangle 2 for Prison bottom
				-0.15f, -0.15f, 0f, 1f, 0f, 1f, 0f,
				-0.15f, -0.15f, 0.1f, 1f, 0f, 1f, 0f,
				0.15f, -0.15f, 0.1f, 1f, 0f, 1f, 0f,
				
				//middle Prison
				// Triangle 1 for Prison left
				-0.15f, 0.15f, 0f, 1f, 0f, 1f, 0f,
				-0.15f, -0.15f, 0f, 1f, 0f, 1f, 0f,
				-0.15f, -0.15f, 0.1f, 1f, 0f, 1f, 0f,
				
				// Triangle 2 for Prison left
				-0.15f, 0.15f, 0f, 1f, 0f, 1f, 0f,
				-0.15f, 0.15f, 0.1f, 1f, 0f, 1f, 0f,
				-0.15f, -0.15f, 0.1f, 1f, 0f, 1f, 0f,
				
				// Triangle 1 for Prison left
				-0.15f, -0.15f, 0f, 1f, 0f, 1f, 0f,
				0.15f, -0.15f, 0f, 1f, 0f, 1f, 0f,
				0.15f, -0.15f, 0.1f, 1f, 0f, 1f, 0f,
				
				// Triangle 2 for Prison left
				-0.15f, -0.15f, 0f, 1f, 0f, 1f, 0f,
				-0.15f, -0.15f, 0.1f, 1f, 0f, 1f, 0f,
				0.15f, -0.15f, 0.1f, 1f, 0f, 1f, 0f
				
				};
		
		vertexData = ByteBuffer
				.allocateDirect(tableVerticesWithTriangles.length * BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer();
		vertexData.put(tableVerticesWithTriangles);
	}
	
	@Override
	/*GLSurfaceView calls this when the surface is created. This happens the first
		time our application is run, and it may also be called when the device
		wakes up or when the user switches back to our activity. In*/
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		// TODO Auto-generated method stub
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		
		String vertexShaderSource = TextResourceReader.readTextFileFromResource(context, R.raw.simple_vertex_shader);
		String fragmentShaderSource = TextResourceReader.readTextFileFromResource(context, R.raw.simple_fragment_shader);
		
		int vertexShader = ShaderHelper.compileVertexShader(vertexShaderSource);
		int fragmentShader = ShaderHelper.compileFragmentShader(fragmentShaderSource);
		
		program = ShaderHelper.linkProgram(vertexShader, fragmentShader);
		
		if (LoggerConfig.ON) 
		{
			ShaderHelper.validateProgram(program);
		}
		
		glUseProgram(program);
		
//		location numbers are used to send data to the shader
		aColorLocation = glGetAttribLocation(program, A_COLOR);
		aPositionLocation = glGetAttribLocation(program, A_POSITION);
		
		vertexData.position(0);
		glVertexAttribPointer(aPositionLocation, POSITION_COMPONENT_COUNT, GL_FLOAT,
																false, STRIDE, vertexData);
		
		glEnableVertexAttribArray(aPositionLocation);
		
		vertexData.position(POSITION_COMPONENT_COUNT);
		glVertexAttribPointer(aColorLocation, COLOR_COMPONENT_COUNT, GL_FLOAT,
		false, STRIDE, vertexData);
		glEnableVertexAttribArray(aColorLocation);
		
		uMatrixLocation = glGetUniformLocation(program, U_MATRIX);
	}
	
	/*GLSurfaceView calls this after the surface is created and whenever the size
		has changed. A size change can occur when switching from portrait to
		landscape and vice versa.*/
	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		// TODO Auto-generated method stub
		glViewport(0, 0, width, height);
		
		MatrixHelper.perspectiveM(projectionMatrix, 60, (float) width
				/ (float) height, 1f, 10f);
//		final float aspectRatio = width > height ?
//				(float) width / (float) height :
//				(float) height / (float) width;
//				
//		if (width > height) 
//		{
//			 // Landscape
//			 orthoM(projectionMatrix, 0, -aspectRatio, aspectRatio, -1f, 1f, -1f, 1f);
//		} 
//		else 
//		{
//			// Portrait or square
//			orthoM(projectionMatrix, 0, -1f, 1f, -aspectRatio, aspectRatio, -1f, 1f);
//		}
		setIdentityM(modelMatrix, 0);
		translateM(modelMatrix, 0, 0f, 0f, -4f);
		rotateM(modelMatrix, 0, -80f, 1f, 0f, 1f);
		
		final float[] temp = new float[16];
		multiplyMM(temp, 0, projectionMatrix, 0, modelMatrix, 0);
		System.arraycopy(temp, 0, projectionMatrix, 0, temp.length);
	}

	@Override
	/*GLSurfaceView calls this when it’s time to draw a frame*/
	public void onDrawFrame(GL10 gl) {
		// TODO Auto-generated method stub
		glClear(GL_COLOR_BUFFER_BIT);
		
		glUniformMatrix4fv(uMatrixLocation, 1, false, projectionMatrix, 0);
		//update the value of u_Color in our shader code
		//glUniform4f(uColorLocation, 1.0f, 1.0f, 1.0f, 1.0f);
		/*The first argument tells OpenGL that we want to
			draw triangles. To draw triangles, we need to pass in at least three vertices
			per triangle. The second argument tells OpenGL to read in vertices starting
			at the beginning of our vertex array, and the third argument tells OpenGL to
			read in six vertices*/
		glDrawArrays(GL_TRIANGLES, 0, 24);
	
		
		
		//glUniform4f(uColorLocation, 1.0f, 0.0f, 0.0f, 1.0f);
		//glDrawArrays(GL_LINES, 6, 2);
		
//		
//		// Draw the first mallet blue.
//		//glUniform4f(uColorLocation, 0.0f, 0.0f, 1.0f, 1.0f);
//		glDrawArrays(GL_POINTS, 8, 1);
//		// Draw the second mallet red.
//		//glUniform4f(uColorLocation, 1.0f, 0.0f, 0.0f, 1.0f);
//		glDrawArrays(GL_POINTS, 9, 1);
	}
	
	

}
