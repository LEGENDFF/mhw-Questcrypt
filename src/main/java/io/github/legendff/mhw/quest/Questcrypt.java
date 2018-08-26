package io.github.legendff.mhw.quest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Questcrypt {
	private static final Key SECRET_KEY = new SecretKeySpec("TZNgJfzyD2WKiuV4SglmI6oN5jP2hhRJcBwzUooyfIUTM4ptDYGjuRTP".getBytes(StandardCharsets.UTF_8), "Blowfish");
	
	public static byte[] changeEndianness(byte[] array) {
		byte[] newArray = new byte[array.length];

		for(int i = 0; i < array.length; i += 4) {
			newArray[i] = array[i+3];
			newArray[i+1] = array[i+2];
			newArray[i+2] = array[i+1];
			newArray[i+3] = array[i];
		}
		
		return newArray;
	}

	public static boolean isDecrypted(byte[] quest) {
		return quest[0] == 0x4C && quest[1] == 0x09 && quest[2] == 0 && quest[3] == 0;
	}

	private static byte[] doCrypto(byte[] quest, int opmode) {
		try {
			Cipher cipher = Cipher.getInstance("Blowfish/ecb/nopadding");
			cipher.init(opmode, SECRET_KEY);
			return changeEndianness(cipher.doFinal(changeEndianness(quest)));
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static byte[] encrypt(byte[] quest) {
		return doCrypto(quest, Cipher.ENCRYPT_MODE);
	}
	
	public static byte[] decrypt(byte[] quest) {
		return doCrypto(quest, Cipher.DECRYPT_MODE);
	}
	
	public static void main(String[] args) throws IOException {
		final Path questFiles = Paths.get(args[0]);
		Files.createDirectories(Paths.get(args[1]));
		Files.walk(questFiles).filter(f -> f.toString().endsWith(".mib")).forEach(questFile -> {
			try {
				byte[] quest = Files.readAllBytes(questFile);
				quest = isDecrypted(quest) ? encrypt(quest) : decrypt(quest);
				Path outputFile = Paths.get(args[1], questFile.getFileName().toString());
				Files.write(outputFile, quest);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}
}