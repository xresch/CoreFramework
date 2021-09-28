package com.xresch.cfw.utils;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;

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
	private static final String[] exaggeratingAdjectives = new String[] { "utterly arduous", "superfluous", "chocolate-addicted", "super-sneaky", "ultra cuddly", "mega religious", "totally angry", "absolutely arrogant", "totally-at-the-ready", "bat-sh*t-crazy", "bull-headed", "100% confused", "fully-cruel-hearted", "over-demanding", "fiercely loyal", "endlessly flirting", "free-loading", "frisky", "god-mode-greedy", "devil-like hateful", "house-broken", "above hyperactive", "high-end", "idiotic", "infuriating", "awfully insecure", "hilariously maniacal", "ultra narrow-minded", "out-of-control", "rebellious", "self-absorbed", "shaky", "shivering", "slippery", "stubborn", "territorial", "tripping", "twisted", "underhanded", "vengeful", "vile", "yapping", "zippy", "zombie-like" };
	
	private static final String[] noMoreIssueMessages = 
		new String[] {
			  "No more issues detected, the robo-brain sending you this message wishes you a marvelous day!"
			, "All issues have been marked as exterminated by the allmighty controlling algorithms. Now you may sit back and relax."
			, "Issues previously reported have abadoned their duties and have vanished to unknown lands. Get a cup of coffee and ensconce yourself."
			, "The alerting controlling unit hereby transmits you the resolve of all and any issues."
			, "Issue target have been detected and successfully eliminated by whoever has decided to touch his keyboard. If you ever find out who it was, you might buy him a pizza."
			, "Behold! Thy issues hav succumbed to the holy digital knights! Thou shall now be at ease."
			, "Code-A57-41: All issues solved by washing the machine with regular soap and greasing the software with virtual oil."
			, "Encountered a state of issue-non-existence. You might need to create no problems to get further messages."
			, "The space reserved for issues has been completely emptied. Any new incoming issues will be assigned to the dedicated space by automated means."
			, "New chemical solution has been observed to be highly corrosive when applied to any system issues. All issues have dissolved, you may now create new ones."
			, "We are glad to inform you that experimental explosives have detonated your application issues."
			, "A little boy has stolen your issues and sold them on the black market. You may aquire new issues in the darknet."
			, "Your wish to remediate the application issues has been granted by a sudo-fairy. The wish was deducted from your magic account."
			, "Robbers had invaded your system. Lucky you the only thing they have stolen where the issues."
			, "Issues have been blown away by an industrious weak breeze. "
			, "Your mafia has burned your issues with high-quality lava from Sicilia."
			, "A gang of rockers have cornered the issues in a backyard. The issue has given up and moved out of your system."
			, "The system police found the culprit in your system an your application issues should have resolved by now."
			, "A small animal with purple fur has sniffed out the issue, catched it and swallowed it wholly without even chewing it once."
			, "A system engineer kicked twice against the physical hardware. This will teach the system not to run into issues anymore."
			, "Seventy-six cats have invaded your system. The furballs have been removed from the relays and your application is now purring again."
			, "After arduous investigations, we applied the same resolution as to the antepenultimate issue. Your system should now be running like nothing ever happened."
		};
	
	/*4 Random integer between 0 and 9999 generated at startup. Useful to make sure content is reloaded after startup.*/
	public static final int STARTUP_RANDOM_INT = randomFromZeroToInteger(9999);
	
	/*4 Random alphanumerical characters generated at startup. Useful to make sure content is reloaded after startup.*/
	public static final String STARTUP_RANDOM_ALPHANUM = randomStringAlphaNumerical(4);
	
	
	public static Random getInstance() { return random;}
	
	public static boolean randomBoolean() { return random.nextInt(100) > 50 ? true : false; }
	
	public static String randomFromArray(String[] array) {
	    int index = random.nextInt(array.length);
	    return array[index];
	}
	
	
	public static String randomFirstnameOfGod() { return randomFromArray(firstnameGods); }
	public static String randomLastnameSweden() { return randomFromArray(lastnameSweden); }
	public static String randomMythicalLocation() { return randomFromArray(mythicalLocations); }
	public static String randomExaggaratingAdjective() { return randomFromArray(exaggeratingAdjectives); }
	public static String randomNoMoreIssueMessages() { return randomFromArray(noMoreIssueMessages); }
	
	
	/******************************************************************************
	 * Creates a random json array of people with various properties.
	 * 
	 * @param count
	 ******************************************************************************/
	public static String[] randomArrayOfExaggaratingAdjectives(int count) { 
		
		String[] stringArray = new String[count];
			
		for(int i = 0; i < count; i++) {
			stringArray[i] = randomExaggaratingAdjective();
		}
		
		return stringArray;
	}
	/******************************************************************************
	 * Creates a random json array of people with various properties.
	 * 
	 * @param count
	 ******************************************************************************/
	public static JsonArray randomJSONArrayOfMightyPeople(int count) { 
		JsonArray array = new JsonArray();
				
		for(int i = 0; i < count; i++) {
			array.add(randomJSONObjectMightyPerson());
		}
		
		return array;
	}
	
	/******************************************************************************
	 * Creates a random json array of people with various properties.
	 * 
	 * @param count
	 ******************************************************************************/
	public static JsonObject randomJSONObjectMightyPerson() { 
		long currentTime = new Date().getTime();
		
		JsonObject object = new JsonObject();
		
		object.addProperty("FIRSTNAME", CFW.Random.randomFirstnameOfGod());
		object.addProperty("LASTNAME", CFW.Random.randomLastnameSweden());
		object.addProperty("LOCATION", CFW.Random.randomMythicalLocation());
		object.addProperty("ID", CFW.Random.randomStringAlphaNumerical(16));
		object.addProperty("LIKES_TIRAMISU", CFW.Random.randomBoolean());
		object.addProperty("LAST_LOGIN", currentTime-(CFW.Random.randomIntegerInRange(100, 10000)*1000000) );
		object.addProperty("VALUE", CFW.Random.randomIntegerInRange(1, 100));
			
		return object;
	}
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
