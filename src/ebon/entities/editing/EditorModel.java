package ebon.entities.editing;

import ebon.entities.*;
import ebon.entities.components.*;
import ebon.entities.loading.*;
import flounder.helpers.*;
import flounder.logger.*;
import flounder.maths.vectors.*;
import flounder.models.*;
import flounder.resources.*;
import flounder.textures.*;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.io.*;

public class EditorModel extends IEditorComponent {
	public ComponentModel component;

	private MyFile pathModel;
	private MyFile pathTexture;
	private MyFile pathNormalMap;

	public EditorModel(Entity entity) {
		this.component = new ComponentModel(entity, null, 1.0f, Texture.newTexture(new MyFile(MyFile.RES_FOLDER, "undefined.png")).create(), null, 0);
	}

	public EditorModel(IEntityComponent component) {
		this.component = (ComponentModel) component;
	}

	@Override
	public String getTabName() {
		return "Model";
	}

	@Override
	public ComponentModel getComponent() {
		return component;
	}

	@Override
	public void addToPanel(JPanel panel) {
		// Load Model.
		JButton loadModel = new JButton("Select Model");
		loadModel.addActionListener((ActionEvent ae) -> {
			JFileChooser fileChooser = new JFileChooser();
			File workingDirectory = new File(System.getProperty("user.dir"));
			fileChooser.setCurrentDirectory(workingDirectory);
			int returnValue = fileChooser.showOpenDialog(null);

			if (returnValue == JFileChooser.APPROVE_OPTION) {
				String selectedFile = fileChooser.getSelectedFile().getPath().replace("\\", "/");

				if (selectedFile.contains("res/entities")) {
					String[] filepath = selectedFile.split("/");
					EditorModel.this.pathModel = new MyFile(MyFile.RES_FOLDER, "entities", filepath[filepath.length - 1].replace(".obj", ""), filepath[filepath.length - 1]);
				} else {
					FlounderLogger.error("The selected model path is not inside the res/entities folder!");
				}
			}
		});
		panel.add(loadModel);

		// Load Texture.
		JButton loadTexture = new JButton("Select Texture");
		loadTexture.addActionListener((ActionEvent ae) -> {
			JFileChooser fileChooser = new JFileChooser();
			File workingDirectory = new File(System.getProperty("user.dir"));
			fileChooser.setCurrentDirectory(workingDirectory);
			int returnValue = fileChooser.showOpenDialog(null);

			if (returnValue == JFileChooser.APPROVE_OPTION) {
				String selectedFile = fileChooser.getSelectedFile().getPath().replace("\\", "/");

				if (selectedFile.contains("res/entities")) {
					String[] filepath = selectedFile.split("/");
					EditorModel.this.pathTexture = new MyFile(MyFile.RES_FOLDER, "entities", filepath[filepath.length - 1].replace(".png", ""), filepath[filepath.length - 1]);
				} else {
					FlounderLogger.error("The selected texture path is not inside the res/entities folder!");
				}
			}
		});
		panel.add(loadTexture);

		// Load Normal Map.
		JButton loadNormalMap = new JButton("Select Normal Map");
		loadNormalMap.addActionListener((ActionEvent ae) -> {
			JFileChooser fileChooser = new JFileChooser();
			File workingDirectory = new File(System.getProperty("user.dir"));
			fileChooser.setCurrentDirectory(workingDirectory);
			int returnValue = fileChooser.showOpenDialog(null);

			if (returnValue == JFileChooser.APPROVE_OPTION) {
				String selectedFile = fileChooser.getSelectedFile().getPath().replace("\\", "/");

				if (selectedFile.contains("res/entities")) {
					String[] filepath = selectedFile.split("/");
					EditorModel.this.pathNormalMap = new MyFile(MyFile.RES_FOLDER, "entities", filepath[filepath.length - 1].replace("Normals.png", ""), filepath[filepath.length - 1]);
				} else {
					FlounderLogger.error("The selected texture path is not inside the res/entities folder!");
				}
			}
		});
		panel.add(loadNormalMap);

		// Scale Slider.
		//	panel.add(new JLabel("Scale Slider: "));
		JSlider scaleSlider = new JSlider(JSlider.HORIZONTAL, 0, 150, (int) (component.getScale() * 25.0f));
		scaleSlider.setToolTipText("Model Scale");
		scaleSlider.addChangeListener((ChangeEvent e) -> {
			JSlider source = (JSlider) e.getSource();
			int reading = source.getValue();
			component.setScale(reading / 25.0f);
		});
		scaleSlider.setMajorTickSpacing(25);
		scaleSlider.setMinorTickSpacing(10);
		scaleSlider.setPaintTicks(true);
		scaleSlider.setPaintLabels(true);
		panel.add(scaleSlider);
	}

	@Override
	public void update() {
		if (component != null) {
			if (pathModel != null && (component.getModel() == null || !component.getModel().getFile().equals(pathModel.getPath()))) {
				if (pathModel.getPath().contains(".obj")) {
					Model model = Model.newModel(pathModel).create();
					component.setModel(model);
				}
			}

			if (pathTexture != null && (component.getTexture() == null || !component.getTexture().getFile().getPath().equals(pathTexture.getPath()))) {
				Texture texture = Texture.newTexture(pathTexture).create();
				component.setTexture(texture);
			}

			if (pathNormalMap != null && (component.getNormalMap() == null || !component.getNormalMap().getFile().getPath().equals(pathNormalMap.getPath()))) {
				Texture normalTexture = Texture.newTexture(pathNormalMap).create();
				component.setNormalMap(normalTexture);
			}
		}
	}

	@Override
	public Pair<String[], EntitySaverFunction[]> getSavableValues(String entityName) {
		if (component.getTexture() != null) {
			try {
				File file = new File("entities/" + entityName + "/" + entityName + ".png");

				if (file.exists()) {
					file.delete();
				}

				file.createNewFile();

				InputStream input = component.getTexture().getFile().getInputStream();
				OutputStream output = new FileOutputStream(file);
				byte[] buf = new byte[1024];
				int bytesRead;

				while ((bytesRead = input.read(buf)) > 0) {
					output.write(buf, 0, bytesRead);
				}

				input.close();
				output.close();
			} catch (IOException e) {
				FlounderLogger.exception(e);
			}
		}

		if (component.getNormalMap() != null) {
			try {
				File file = new File("entities/" + entityName + "/" + entityName + "Normals.png");

				if (file.exists()) {
					file.delete();
				}

				file.createNewFile();

				InputStream input = component.getNormalMap().getFile().getInputStream();
				OutputStream output = new FileOutputStream(file);
				byte[] buf = new byte[1024];
				int bytesRead;

				while ((bytesRead = input.read(buf)) > 0) {
					output.write(buf, 0, bytesRead);
				}

				input.close();
				output.close();
			} catch (IOException e) {
				FlounderLogger.exception(e);
			}
		}

		EntitySaverFunction saveVertices = new EntitySaverFunction("Vertices") {
			@Override
			public void writeIntoSection(FileWriterHelper entityFileWriter) throws IOException {
				if (component.getModel() != null) {
					for (float v : component.getModel().getMeshData().getVertices()) {
						String s = v + ",";
						entityFileWriter.writeSegmentData(s);
					}
				}
			}
		};
		EntitySaverFunction saveTextureCoords = new EntitySaverFunction("TextureCoords") {
			@Override
			public void writeIntoSection(FileWriterHelper entityFileWriter) throws IOException {
				if (component.getModel() != null) {
					for (float v : component.getModel().getMeshData().getTextures()) {
						String s = v + ",";
						entityFileWriter.writeSegmentData(s);
					}
				}
			}
		};
		EntitySaverFunction saveNormals = new EntitySaverFunction("Normals") {
			@Override
			public void writeIntoSection(FileWriterHelper entityFileWriter) throws IOException {
				if (component.getModel() != null) {
					for (float v : component.getModel().getMeshData().getNormals()) {
						String s = v + ",";
						entityFileWriter.writeSegmentData(s);
					}
				}
			}
		};
		EntitySaverFunction saveTangents = new EntitySaverFunction("Tangents") {
			@Override
			public void writeIntoSection(FileWriterHelper entityFileWriter) throws IOException {
				if (component.getModel() != null) {
					for (float v : component.getModel().getMeshData().getTangents()) {
						String s = v + ",";
						entityFileWriter.writeSegmentData(s);
					}
				}
			}
		};
		EntitySaverFunction saveIndices = new EntitySaverFunction("Indices") {
			@Override
			public void writeIntoSection(FileWriterHelper entityFileWriter) throws IOException {
				if (component.getModel() != null) {
					for (int i : component.getModel().getMeshData().getIndices()) {
						String s = i + ",";
						entityFileWriter.writeSegmentData(s);
					}
				}
			}
		};
		EntitySaverFunction saveAABB = new EntitySaverFunction("AABB") {
			@Override
			public void writeIntoSection(FileWriterHelper entityFileWriter) throws IOException {
				if (component.getModel() != null && component.getModel().getMeshData() != null && component.getModel().getMeshData().getAABB() != null) {
					Vector3f min = component.getModel().getMeshData().getAABB().getMinExtents();
					Vector3f max = component.getModel().getMeshData().getAABB().getMaxExtents();
					String s = min.x + "," + min.y + "," + min.z + "," + max.x + "," + max.y + "," + max.z + ",";
					entityFileWriter.writeSegmentData(s);
				}
			}
		};
		EntitySaverFunction saveQuickHull = new EntitySaverFunction("QuickHull") {
			@Override
			public void writeIntoSection(FileWriterHelper entityFileWriter) throws IOException {
				if (component.getModel() != null && component.getModel().getMeshData() != null && component.getModel().getMeshData().getHull() != null && component.getModel().getMeshData().getHull().getHullPoints() != null) {
					for (Vector3f v : component.getModel().getMeshData().getHull().getHullPoints()) {
						String s = v.x + "," + v.y + "," + v.z + ",";
						entityFileWriter.writeSegmentData(s);
					}
				}
			}
		};

		String saveScale = "Scale: " + component.getScale();

		String saveTexture = "Texture: " + (component.getTexture() == null ? null : "res/entities/" + entityName + "/" + entityName + ".png");
		String saveTextureNumRows = "TextureNumRows: " + (component.getTexture() == null ? 1 : component.getTexture().getNumberOfRows());

		String saveNormalMap = "NormalMap: " + (component.getNormalMap() == null ? null : "res/entities/" + entityName + "/" + entityName + "Normals.png");
		String saveNormalMapNumRows = "NormalMapNumRows: " + (component.getNormalMap() == null ? 1 : component.getNormalMap().getNumberOfRows());

		return new Pair<>(
				new String[]{saveScale, saveTexture, saveTextureNumRows, saveNormalMap, saveNormalMapNumRows},
				new EntitySaverFunction[]{saveVertices, saveTextureCoords, saveNormals, saveTangents, saveIndices, saveAABB, saveQuickHull}
		);
	}
}
