package net.gmx.nosefish.fishysigns.plugin.engine;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import net.gmx.nosefish.fishysigns.Log;
import net.gmx.nosefish.fishysigns.annotation.FishySignIdentifier;
import net.gmx.nosefish.fishysigns.signs.FishySign;


public final class FishySignClassLoader {
	private static FishySignClassLoader instance = new FishySignClassLoader();
	private static String FISHYSIGNDIR = "plugins/fishysigns";
	// known FishySign classes
	private Map<SignRegEx, Class<? extends FishySign>> signTypes = 
			new ConcurrentHashMap<SignRegEx, Class<? extends FishySign>>();

	public static FishySignClassLoader getInstance() {
		return instance;
	}
	
	/**
	 * Load classes that extend FishySign from jars in plugins directory.
	 */
	// This method only searches for jars.
	// The actual loading happens in loadAllFishySignClassesFromJar.
	public void loadAllFishySignClasses() {
		signTypes.clear();
		File signDir = new File(FISHYSIGNDIR);
		if (!signDir.exists()) {
			Log.get().logSevere(
					"No FishySigns found. '" + FISHYSIGNDIR
							+ "' is not a directory. Creating...");
			signDir.mkdir();
			return;
		} else if (!signDir.isDirectory()) {
			Log.get().logSevere(
					"No FishySigns found. '" + FISHYSIGNDIR
							+ "' is not a directory but a file.");
			return;
		}
		Log.get().logInfo("Loading FishySign classes from jars in " + signDir.getAbsolutePath().toString());
		FilenameFilter onlyJarFiles = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".jar");
			}
		};
		for (File jarFile : signDir.listFiles(onlyJarFiles)) {
			loadAllFishySignClassesFromJar(jarFile.getAbsolutePath());
		}
	}
	
	/**
	 * The actual loading.
	 * @param pathToJar
	 */
	private void loadAllFishySignClassesFromJar(String pathToJar) {
		try {
			JarFile jarFile = new JarFile(pathToJar);

			URL[] urls = { new URL("jar:file:" + pathToJar+"!/") };
			ClassLoader loader = URLClassLoader.newInstance(urls, FishySignClassLoader.class.getClassLoader());

			Enumeration<JarEntry> jarEntries = jarFile.entries();
			while (jarEntries.hasMoreElements()) {
				JarEntry jarEntry = jarEntries.nextElement();
				if(jarEntry.isDirectory() || !jarEntry.getName().endsWith(".class")){
					continue;
				}
				// -6 because of .class
				String className = jarEntry.getName().substring(0,jarEntry.getName().length()-6);
				className = className.replace('/', '.');
				try {
					Class<?> loadedClass = loader.loadClass(className);
					if (FishySign.class.isAssignableFrom(loadedClass)) {
						SignRegEx regEx = getIdentifierRegEx(loadedClass);
						if (regEx != null) {
							signTypes.put(regEx,
									loadedClass.asSubclass(FishySign.class));
							Log.get().logInfo("Loaded FishySign class " + className);
						} else {
							Log.get().logWarning(
									"Could not load FishySign class " + className
									+ " - no @FishySignIdentifier found.");
						}
					} else {
						// not subclass of FishySign.
						//Log.get().logWarning(className + " in " + pathToJar + " is not a subclass of FishySign");
					}
				} catch (ClassNotFoundException cnf) {
					// for some strange reason, the class has disappeared from the jar
					//Log.get().logStacktrace("Could not load class from jar", cnf);
				}
			}
		} catch (IOException e) {
			// error accessing the jar
			//Log.get().logStacktrace("Could not load class from jar", e);
		}
	}

	/**
	 * Gets the set of regex <code>Pattern</code>s that identify this sign class form the annotated static field.
	 * @param cls the class to process.
	 * @return the Pattern[] that can be used to identify signs of this class, or null if no <code>@FishySignIdentifier</code> was found
	 */
	private SignRegEx getIdentifierRegEx(Class<?> cls) {
		Field[] fields = cls.getFields();
		for (Field field : fields) {
			if (field.getAnnotation(FishySignIdentifier.class) != null) {
				try {
					Object value = field.get(null);
					if (value instanceof Pattern[]) {
						return new SignRegEx((Pattern[]) value);
					}
				} catch (IllegalArgumentException e) {
					Log.get().logStacktrace(
							"Exception while loading FishySignIdentifier", e);
				} catch (IllegalAccessException e) {
					Log.get().logStacktrace(
							"Exception while loading FishySignIdentifier", e);
				}
			}
		}
		return null;
	}

	/**
	 * Determines a suitable subclass of <code>FishySign</code> from the sign text.
	 * @param signText
	 * @return the class that has a matching <code>@FishySignIdentifier</code>
	 */
	public Class<? extends FishySign> getFishyClass(String[] signText) {
		if (signTypes != null) {
			for (Entry<SignRegEx, Class<? extends FishySign>> entry : signTypes.entrySet()) {
				if (entry.getKey().matches(signText)) {
					return entry.getValue();
				}
			}
		}
		return null;
	}
	
	/**
	 * Factory method to instantiate a FishySign from the sign text.
	 * @param sign
	 * @return a new <code>FishySign</code> instance of a subclass that that matches the text, or null if none was found.
	 */
	public FishySign makeFishySign(UnloadedSign sign) {
		FishySign fishySign = null;
		// find matching class
		Class<? extends FishySign> signClass = this.getFishyClass(sign.getText());
		if (signClass != null) {
			try {
				// instantiate
				Constructor<? extends FishySign> signConstructor = signClass.getConstructor(UnloadedSign.class); 
				fishySign = signConstructor.newInstance(sign);
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return fishySign;
	}
}
