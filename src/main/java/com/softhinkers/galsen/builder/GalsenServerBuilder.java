package com.softhinkers.galsen.builder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.softhinkers.galsen.GalsenConfiguration;
import com.softhinkers.galsen.android.AndroidApp;
import com.softhinkers.galsen.android.AndroidSdk;
import com.softhinkers.galsen.android.DefaultAndroidApp;
import com.softhinkers.galsen.android.JavaSdk;
import com.softhinkers.galsen.exceptions.AndroidSdkException;
import com.softhinkers.galsen.exceptions.GalsenException;
import com.softhinkers.galsen.exceptions.ShellCommandException;
import com.softhinkers.galsen.io.ShellCommand;
import com.softhinkers.galsen.server.model.GalsenStandaloneDriver;

public class GalsenServerBuilder {

	public static final String GALSEN_TEST_APP_PACKAGE = "com.softhinkers.galsen.testapp";
	private static final Logger log = Logger
			.getLogger(GalsenServerBuilder.class.getName());
	public static final String GALSEN_FINAL_NAME = "galsen-server.apk";
	public static final String PREBUILD_GALSEN_SERVER_PATH_PREFIX = "/prebuild/galsen-server-";
	public static final String ANDROID_APPLICATION_XML_TEMPLATE = "/AndroidManifest.xml";
	public static final String ICON = "android:icon=\"@drawable/galsen_icon\"";

	private String galsenPrebuildServerPath;
	private String galsenApplicationXmlTemplate;
	private AndroidApp galsenServer;
	private AndroidApp applicationUnderTest;
	private GalsenConfiguration serverConfiguration;

	GalsenServerBuilder(String galsenPrebuildServerPath,
			String galsenApplicationXmlTemplate) {
		this.galsenPrebuildServerPath = null;
		this.galsenApplicationXmlTemplate = null;
		this.galsenServer = null;
		this.applicationUnderTest = null;
		this.serverConfiguration = null;

		this.galsenPrebuildServerPath = galsenPrebuildServerPath;
		this.galsenApplicationXmlTemplate = galsenApplicationXmlTemplate;
	}

	public GalsenServerBuilder() {
		this(null);
	}

	public GalsenServerBuilder(GalsenConfiguration serverConfiguration) {
		this.galsenPrebuildServerPath = null;
		this.galsenApplicationXmlTemplate = null;
		this.galsenServer = null;
		this.applicationUnderTest = null;
		this.serverConfiguration = null;

		this.galsenPrebuildServerPath = "/prebuild/galsen-server-"
				+ "0.11.0" + ".apk";

		this.galsenApplicationXmlTemplate = "/AndroidManifest.xml";
		this.serverConfiguration = serverConfiguration;
	}

	void init(AndroidApp aut) throws IOException, ShellCommandException {
		this.applicationUnderTest = aut;
		File customizedServer = File.createTempFile("galsen-server", ".apk");

		log.info("Creating customized Galsen-server: "
				+ customizedServer.getAbsolutePath());
		InputStream is = getResourceAsStream(this.galsenPrebuildServerPath);

		IOUtils.copy(is, new FileOutputStream(customizedServer));
		IOUtils.closeQuietly(is);
		this.galsenServer = new DefaultAndroidApp(customizedServer);
	}

	public AndroidApp createGalsenServer(AndroidApp aut) throws IOException,
			ShellCommandException, AndroidSdkException {
		log.info("create GalsenServer for apk: " + aut.getAbsolutePath());
		init(aut);
		cleanUpPrebuildServer();
		File galsenServer = createAndAddCustomizedAndroidManifestToGalsenServer();
		File outputFile = new File(FileUtils.getTempDirectory(), String.format(
				"galsen-server-%s-%s.apk", new Object[] {
						this.applicationUnderTest.getBasePackage(),
						getJarVersionNumber() }));

		return signTestServer(galsenServer, outputFile);
	}



	public AndroidApp resignApp(File appFile) throws ShellCommandException,
			AndroidSdkException {
		AndroidApp app = new DefaultAndroidApp(appFile);

		deleteFileFromAppSilently(app, "META-INF/MANIFEST.MF");
		deleteFileFromAppSilently(app, "META-INF/CERT.RSA");
		deleteFileFromAppSilently(app, "META-INF/CERT.SF");
		deleteFileFromAppSilently(app, "META-INF/ANDROIDD.SF");
		deleteFileFromAppSilently(app, "META-INF/ANDROIDD.RSA");
		deleteFileFromAppSilently(app, "META-INF/NDKEYSTO.SF");
		deleteFileFromAppSilently(app, "META-INF/NDKEYSTO.RSA");

		File outputFile = new File(appFile.getParentFile(), "resigned-"
				+ appFile.getName());
		return signTestServer(appFile, outputFile);
	}
	
	private void deleteFileFromAppSilently(AndroidApp app, String file)
			throws AndroidSdkException {
		if (app == null) {
			throw new IllegalArgumentException(
					"Required parameter 'app' is null.");
		}
		if ((file == null) || (file.isEmpty()))
			throw new IllegalArgumentException(
					"Required parameter 'file' is null or empty.");
		try {
			app.deleteFileFromWithinApk(file);
		} catch (ShellCommandException e) {
		}
	}

	File createAndAddCustomizedAndroidManifestToGalsenServer()
			throws IOException, ShellCommandException, AndroidSdkException {
		String targetPackageName = this.applicationUnderTest.getBasePackage();
		File tempdir = new File(FileUtils.getTempDirectoryPath()
				+ File.separatorChar + targetPackageName
				+ System.currentTimeMillis());

		if (!(tempdir.exists())) {
			tempdir.mkdirs();
		}

		File customizedManifest = new File(tempdir, "AndroidManifest.xml");
		log.info("Adding target package '" + targetPackageName + "' to "
				+ customizedManifest.getAbsolutePath());

		InputStream inputStream = getResourceAsStream(this.galsenApplicationXmlTemplate);
		if (inputStream == null) {
			throw new GalsenException(
					"AndroidApplication.xml template file was not found.");
		}
		String content = IOUtils.toString(inputStream, Charset.defaultCharset()
				.displayName());

		int i = content.toLowerCase().indexOf("package");
		int cnt = 0;
		for (; i < content.length(); ++i) {
			if (content.charAt(i) == '"') {
				++cnt;
			}
			if (cnt == 2) {
				break;
			}
		}
		content = content.substring(0, i) + "." + targetPackageName
				+ content.substring(i);
		log.info("Final Manifest File:\n" + content);
		content = content.replaceAll("com.softhinkers.galsen.testapp",
				targetPackageName);

		if (content.contains("android:icon=\"@drawable/galsen_icon\"")) {
			content = content.replaceAll(
					"android:icon=\"@drawable/galsen_icon\"", "");
		}

		OutputStream outputStream = new FileOutputStream(customizedManifest);
		IOUtils.write(content, outputStream, Charset.defaultCharset()
				.displayName());
		IOUtils.closeQuietly(inputStream);
		IOUtils.closeQuietly(outputStream);

		CommandLine createManifestApk = new CommandLine(AndroidSdk.aapt());

		createManifestApk.addArgument("package", false);
		createManifestApk.addArgument("-M", false);
		createManifestApk.addArgument(customizedManifest.getAbsolutePath(),
				false);
		createManifestApk.addArgument("-I", false);
		createManifestApk.addArgument(AndroidSdk.androidJar(), false);
		createManifestApk.addArgument("-F", false);
		createManifestApk.addArgument(tempdir.getAbsolutePath()
				+ File.separatorChar + "manifest.apk", false);

		createManifestApk.addArgument("-f", false);
		log.info(ShellCommand.exec(createManifestApk, 20000L));

		ZipFile manifestApk = new ZipFile(new File(tempdir.getAbsolutePath()
				+ File.separatorChar + "manifest.apk"));

		ZipArchiveEntry binaryManifestXml = manifestApk
				.getEntry("AndroidManifest.xml");

		File finalGalsenServerFile = new File(tempdir.getAbsolutePath()
				+ "galsen-server.apk");
		ZipArchiveOutputStream finalGalsenServer = new ZipArchiveOutputStream(
				finalGalsenServerFile);

		finalGalsenServer.putArchiveEntry(binaryManifestXml);
		IOUtils.copy(manifestApk.getInputStream(binaryManifestXml),
				finalGalsenServer);

		ZipFile galsenPrebuildApk = new ZipFile(
				this.galsenServer.getAbsolutePath());
		Enumeration entries = galsenPrebuildApk.getEntries();
		while (entries.hasMoreElements()) {
			ZipArchiveEntry dd = (ZipArchiveEntry) entries.nextElement();
			finalGalsenServer.putArchiveEntry(dd);

			IOUtils.copy(galsenPrebuildApk.getInputStream(dd),
					finalGalsenServer);
		}

		finalGalsenServer.closeArchiveEntry();
		finalGalsenServer.close();
		manifestApk.close();
		log.info("file: " + finalGalsenServerFile.getAbsolutePath());
		return finalGalsenServerFile;
	}

	AndroidApp signTestServer(File customGalsenServer, File outputFileName)
			throws ShellCommandException, AndroidSdkException {
		if (outputFileName == null) {
			throw new IllegalArgumentException(
					"outputFileName parameter is null.");
		}
		File androidKeyStore = androidDebugKeystore();

		if (!(androidKeyStore.isFile())) {
			CommandLine commandline = new CommandLine(JavaSdk.keytool());

			commandline.addArgument("-genkey", false);
			commandline.addArgument("-v", false);
			commandline.addArgument("-keystore", false);
			commandline.addArgument(androidKeyStore.toString(), false);
			commandline.addArgument("-storepass", false);
			commandline.addArgument("android", false);
			commandline.addArgument("-alias", false);
			commandline.addArgument("androiddebugkey", false);
			commandline.addArgument("-keypass", false);
			commandline.addArgument("android", false);
			commandline.addArgument("-dname", false);
			commandline.addArgument("CN=Android Debug,O=Android,C=US", false);
			commandline.addArgument("-storetype", false);
			commandline.addArgument("JKS", false);
			commandline.addArgument("-sigalg", false);
			commandline.addArgument("MD5withRSA", false);
			commandline.addArgument("-keyalg", false);
			commandline.addArgument("RSA", false);
			commandline.addArgument("-validity", false);
			commandline.addArgument("9999", false);

			String output = ShellCommand.exec(commandline, 20000L);
			log.info("A new keystore has been created: " + output);
		}

		CommandLine commandline = new CommandLine(JavaSdk.jarsigner());

		commandline.addArgument("-sigalg", false);
		commandline.addArgument("MD5withRSA", false);
		commandline.addArgument("-digestalg", false);
		commandline.addArgument("SHA1", false);
		commandline.addArgument("-signedjar", false);
		commandline.addArgument(outputFileName.getAbsolutePath(), false);
		commandline.addArgument("-storepass", false);
		commandline.addArgument("android", false);
		commandline.addArgument("-keystore", false);
		commandline.addArgument(androidKeyStore.toString(), false);
		commandline
				.addArgument(customGalsenServer.getAbsolutePath(), false);
		commandline.addArgument("androiddebugkey", false);
		String output = ShellCommand.exec(commandline, 20000L);
		if (log.isLoggable(Level.INFO)) {
			log.info("App signing output: " + output);
		}
		log.info("The app has been signed: " + outputFileName.getAbsolutePath());
		return new DefaultAndroidApp(outputFileName);
	}

	private File androidDebugKeystore() {
		if ((this.serverConfiguration == null)
				|| (this.serverConfiguration.getKeystore() == null)) {
			return new File(FileUtils.getUserDirectory(), File.separatorChar
					+ ".android" + File.separatorChar + "debug.keystore");
		}

		return new File(this.serverConfiguration.getKeystore());
	}

	void cleanUpPrebuildServer() throws ShellCommandException,
			AndroidSdkException {
		this.galsenServer.deleteFileFromWithinApk("META-INF/CERT.RSA");
		this.galsenServer.deleteFileFromWithinApk("META-INF/CERT.SF");
		this.galsenServer.deleteFileFromWithinApk("AndroidManifest.xml");
	}

	AndroidApp getGalsenServer() {
		return this.galsenServer;
	}

	AndroidApp getApplicationUnderTest() {
		return this.applicationUnderTest;
	}

	private InputStream getResourceAsStream(String resource) {
		InputStream is = null;

		is = super.getClass().getResourceAsStream(resource);

		if (is == null)
			try {
				is = new FileInputStream(new File(resource));
			} catch (FileNotFoundException e) {
			}
		if (is == null) {
			throw new GalsenException("The resource '" + resource
					+ "' was not found.");
		}
		return is;
	}

	public static String getJarVersionNumber() {
		Class clazz = GalsenStandaloneDriver.class;
		String className = clazz.getSimpleName() + ".class";
		String classPath = clazz.getResource(className).toString();
		if (!(classPath.startsWith("jar"))) {
			return "dev";
		}
		String manifestPath = classPath.substring(0,
				classPath.lastIndexOf("!") + 1) + "/META-INF/MANIFEST.MF";

		Manifest manifest = null;
		try {
			manifest = new Manifest(new URL(manifestPath).openStream());
		} catch (Exception e) {
			return "";
		}
		Attributes attr = manifest.getMainAttributes();
		String value = attr.getValue("version");
		return value;
	}

}
