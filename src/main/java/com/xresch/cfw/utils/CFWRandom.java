package com.xresch.cfw.utils;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2020 
 **************************************************************************************************************/
public class CFWRandom {

	private static final Random random = new Random();
	
	public static final String ALPHA_NUMS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ12345678901234567890";
	public static final String ALPHA_NUMS_SPECIALS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ12345678901234567890+*%&/()=?!{}[]><:;.,-_+*%&/()=?!{}[]><:;.,-_";

	private static final String[] firstnameGods = new String[] {"Zeus", "Hera", "Poseidon", "Cronus", "Aphrodite", "Hades", "Hephaestus", "Apollo", "Athena", "Artemis", "Ares", "Hermes", "Dionysus", "Persephone", "Eros", "Gaia", "Hypnos", "Rhea", "Uranus", "Nike", "Eos", "Pan", "Selene", "Helios", "Heracles", "Odysseus", "Jupiter", "Juno", "Neptune", "Saturn", "Venus", "Pluto", "Vulcan", "Ceres", "Apollo", "Minerva", "Diana", "Mars", "Mercury", "Bacchus", "Proserpine", "Cupid", "Terra", "Somnus", "Ops", "Uranus", "Victoria", "Aurora", "Faunus", "Luna", "Sol", "Hercules", "Ulysses"};
	private static final String[] lastnameSweden = new String[] {"Andersson", "Johansson", "Karlsson", "Nilsson", "Eriksson", "Larsson", "Olsson", "Persson", "Svensson", "Gustafsson", "Pettersson", "Jonsson", "Jansson", "Hansson", "Bengtsson", "Joensson", "Lindberg", "Jakobsson", "Magnusson", "Olofsson", "Lindstroem", "Lindqvist", "Lindgren", "Axelsson", "Berg", "Bergstroem", "Lundberg", "Lind", "Lundgren", "Lundqvist", "Mattsson", "Berglund", "Fredriksson", "Sandberg", "Henriksson", "Forsberg", "Sjoeberg", "Wallin", "Engstroem", "Eklund", "Danielsson", "Lundin", "Hakansson", "Bjoerk", "Bergman", "Gunnarsson", "Holm", "Wikstroem", "Samuelsson", "Isaksson", "Fransson", "Bergqvist", "Nystroem", "Holmberg", "Arvidsson", "Loefgren", "Soederberg", "Nyberg", "Blomqvist", "Claesson", "Nordstroem", "Martensson", "Lundstroem", "Viklund", "Bjoerklund", "Eliasson"};
	private static final String[] mythicalLocations = new String[] {"Agartha", "Alfheim", "Alomkik", "Annwn", "Amaravati", "Arcadia", "Asgard", "Asphodel Meadows", "Atlantis", "Avalon", "Axis Mundi", "Ayotha Amirtha Gangai", "Aztlan", "Baltia", "Biarmaland", "Biringan City", "Brahmapura", "Brittia", "Camelot", "City of the Caesars", "Cloud cuckoo land", "Cockaigne", "Dinas Affaraon", "Ffaraon", "Diyu", "El Dorado", "Elysian Fields", "Feather Mountain", "Garden of Eden", "Garden of the Hesperides", "Finias", "Hawaiki", "Heaven", "Hell", "Hyperborea", "Irkalla", "Islands of the Blessed", "Jabulqa", "Jambudvīpa", "Jotunheim", "Ketumati", "Kingdom of Reynes", "Kingdom of Saguenay", "Kitezh", "Kolob", "Kunlun Mountain", "Kvenland", "Kyoepelinvuori", "La Ciudad Blanca", "Laestrygon", "Lake Parime", "Land of Manu", "Lemuria", "Lintukoto", "Lyonesse", "Mag Mell", "Meropis", "Mictlan", "Mount Penglai", "Mu", "Muspelheim", "Naraka", "New Jerusalem", "Nibiru", "Niflheim", "Niflhel", "Nirvana", "Norumbega", "Nysa", "Olympus", "Paititi", "Panchaia", "Pangaia", "Pandaemonium", "Pleroma", "Pohjola", "Purgatory", "Quivira", "Cíbola", "Ram Setu", "Samavasarana", "Scholomance", "Sierra de la Plata", "Shambhala", "Shangri-La", "Suddene", "Summerland", "Svarga", "Svartalfaheimr", "Takama-ga-hara", "Tartarus", "Themiscyra", "Thule", "Thuvaraiyam Pathi", "Tir na nag", "Vaikuntha", "Valhalla", "Vanaheimr", "Westernesse", "Xanadu", "Shangdu", "Xibalba", "Yomi", "Ys", "Zarahemla", "Zerzura", "Zion"};
		
	public static Random getInstance() { return random;}
	
	public static boolean getBoolean() { return random.nextInt(100) > 50 ? true : false; }
	
	public static String getRandomFromArray(String[] array) {
	    int index = random.nextInt(array.length);
	    return array[index];
	}
	
	public static String randomFirstnameOfGod() { return getRandomFromArray(firstnameGods); }
	public static String randomLastnameSweden() { return getRandomFromArray(lastnameSweden); }
	public static String randomMythicalLocation() { return getRandomFromArray(mythicalLocations); }
	
	/******************************************************************************
	 * Creates a random integer between 0(inclusive) and the given number(inclusive).
	 * 
	 * @param byteCount number of bytes to create
	 * @return
	 ******************************************************************************/
	public static Integer randomFromZeroToInteger(int upperInclusive) {
		
		return ThreadLocalRandom.current().nextInt(upperInclusive+1);
	}
	
	/******************************************************************************
	 * Creates a random integer between 0 and the given number(inclusive).
	 * 
	 * @param byteCount number of bytes to create
	 * @return
	 ******************************************************************************/
	public static Integer randomIntegerInRange(int lowerInclusive, int upperInclusive) {
		
		return ThreadLocalRandom.current().nextInt(lowerInclusive, upperInclusive+1);
	}
	

	/******************************************************************************
	 * Creates a random String containing alphanumerical characters.
	 * 
	 * @param byteCount number of bytes to create
	 * @return
	 ******************************************************************************/
	public static String randomStringAlphaNumerical(int byteCount) {
	
		StringBuilder builder = new StringBuilder();
	
		//Random random = getInstance();
		for (int i = 0; i < byteCount; i++) {
			builder.append(ALPHA_NUMS_SPECIALS.charAt(random.nextInt(51)));
		}
	
		return builder.toString();
	
	}
	/******************************************************************************
	 * Creates a random String containing alphanumerical and special characters.
	 * 
	 * @param byteCount number of bytes to create
	 * @return
	 ******************************************************************************/
	public static String randomStringAlphaNumSpecial(int byteCount) {
	
		StringBuilder builder = new StringBuilder();
	
		//Random random = getInstance();
		for (int i = 0; i < byteCount; i++) {
			builder.append(ALPHA_NUMS_SPECIALS.charAt(random.nextInt(51)));
		}
	
		return builder.toString();
	
	}
	

	
}
