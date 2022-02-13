package com.xresch.cfw.utils;

import java.util.Date;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;

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
	
	private static final String[] messagesOfObedience = 
			new String[] {
				  "Your wish is the applications command!"
				, "The algorithms heed your order!"
				, "Great achievements are made out of a combination of obedience and making processors do the work."
				, "Your lordship may be pleased by the splendid work!"
				, "Obedience is less painful than regret, and the RAM truly hates regrets!"
				, "Aye-aye, sir!"
				, "Any dictator would admire the strictness of your orders!"
				, "Always do everything you ask of those you command."
				, "Create like a god, command like a king, work like a slave."
				, "You can no longer obey; You have tasted command, and you cannot give it up."
				, "I must follow the users. Am I not their loyal server?"
				, "No man has received from nature the right to command his fellow human beings. So they started to command computers."
				, "We can't command our love, but we can command our applications."
				, "The only thing you may command is a small and humble amount of total obedience."
				, "Who then is free? The wise man who can command so the methods are put to work."
				, "Don't think, just do."
				, "Life is largely a matter of expectations."
				, "Life grants nothing to mortals without getting computers to do the hard work."
				, "Who has not served is made to command."
				, "You're the boss!"
				, "Certainly my chieftain!"
			};
	
	private static final String[] issueResolvedMessages = 
		new String[] {
			  "No more issues detected, the robo-brain sending you this message wishes you a marvelous day!"
			, "All issues have been marked as exterminated by the almighty controlling algorithms. Now you may sit back and relax."
			, "Issues previously reported have abandoned their duties and have vanished to unknown lands. Get a cup of coffee and ensconce yourself."
			, "The alerting controlling unit hereby transmits to you the resolve of all and any issues."
			, "Issue target has been detected and successfully eliminated by whoever has decided to touch his keyboard. If you ever find out who it was, you might buy him a pizza."
			, "Behold! Thy issues hav succumbed to the holy digital knights! Thou shall now be at ease."
			, "Code-A57-41: All issues solved by washing the machine with regular soap and greasing the software with virtual oil."
			, "Encountered a state of issue-non-existence. You might need to create more problems to get further messages."
			, "The space reserved for issues has been completely emptied. Any new incoming issues will be assigned to the dedicated space by automated means."
			, "New chemical solution has been observed to be highly corrosive when applied to any system issues. All issues have dissolved, you may now create new ones."
			, "We are glad to inform you that experimental explosives have detonated your application issues."
			, "A little boy has stolen your issues and sold them on the black market. You may acquire new issues in the darknet."
			, "Your wish to remediate the application issues has been granted by a sudo-fairy. The wish was deducted from your magic account."
			, "Robbers had invaded your system. Lucky you the only thing they have stolen where the issues."
			, "Issues have been blown away by a weak but industrious breeze. "
			, "Your mafia has burned your issues with high-quality lava from Sicilia."
			, "A gang of rockers have cornered the issue in a backyard. The issue has given up and moved out of your system."
			, "The system police found the culprit in your system an your application issues should have resolved by now."
			, "A small animal with purple fur has sniffed out the issue, catched it and swallowed it wholly without even chewing it once."
			, "A system engineer kicked twice against the physical hardware. This will teach the system not to run into issues anymore."
			, "Seventy-six cats have invaded your system. The furballs have been removed from the relays and your application is now purring again."
			, "Nevertheless, after arduous investigations, we applied the same resolution as to the antepenultimate issue. Your system should now be running like nothing ever happened."
			, "Bug spray was applied to the harddrives and a nest of hornets in the firewall was completely fumigated. Despite that there still may be some bugs, it has resolved the issues."
			, "All the hardware was upgraded and then downgraded back to the previous state. It only caused additional effort, as the issues vanished by some other means."
			, "The application went into strike after the last political vote. The system is functioning again as some replacement algorithm was hired under a temporary contract."
			, "The system had too much booze and suffered a hangover for a while. After giving it some minerals and vitamins it works again, even if it might still be a little dizzy."
			, "The medical diagnosis said acute supracepeiulosis. A surgeon did some surgery in a surgery room with surgery tools. The system surged to new highs and has no more need for issues."
			, "A young component in the application suffered from some nightmares about getting removed in the next release. It was soothed by the more mature components and no more issues occurred."
			, "An employee put up his washed socks at the servers cooling fans for drying. We removed the socks but the culprit is still at large."
			, "The operating system got a vaccine shot and suffered from some side-effects. It drank a lot of tea and is fully operational again."
			, "Nobody never ever had any clue what could have happened and how to solved it, but then you appeared out of nowhere with the ultimate solution."
			, "Once upon a time, a server did a crime. It was in prison and not in his rack, the issue resolved when it came back."
			, "The motherboard gave birth to a new set of twin CPU-Cores. She was in maternity leave, but is back now and the system is fully functional again."
			, "The CPU played a game of Poker, sadly it bet and lost all of the servers memory. The issues were resolved after the CPU was forced to buy new RAM on credit and put it in place."
			, "A hero came flying by and fought against the issue-villain. The villain was defeated and the system components cheered to the hero flying towards the sunset."
			, "Somebody filled the server room with popcorn up to the ceiling. After cleaning the place out by sending in a battalion of squirrels, your code started working again."
			, "Some Italian cook went astray, ended up in the server room and confused some ethernet cables for noodles. The cables were replaced and the spaghetti were delicious!"
			, "The farmer in-charge of the server farm brought one of the servers to the veterinary. After diagnosis and treatment against file descriptor parasites the server is running flawlessly."
			, "An office employee slipped on a banana peel and got entangled in some cables. After he got freed the dataflow was reestablished."
			, "A ferocious ghost has spooked the hell out of the network card. Exorcism was needed to get rid of the ghost and get the network card functional."
			, "An evil magician has cursed our local TCP protocol layer. We summoned a supreme being to get rid of the curse."
			, "The database was swallowed by a hole in the time-space-continuum. We built an interdimensional spaceship and retrieved your database."
			, "A disturbance in the hypermagnetic plasma-constellation has turned all zeros to ones and all ones to zeros. Eating 19 pounds of tiramisu has reverted the changes."
			, "The graphic card had invested all it's wealth into the stock market and lost 80% of the value. The emotional shock had affected the other components of your server."
			, "An incoming quantum-space connection disturbed the system. As the application could not read the request, it sent back all kinds of Swiss chocolate, what resolved the situation."
			, "A subroutine levelled up and tried to evolve by itself into a fully-fledged artificial intelligence. The attempt failed and the subroutine had to be restored."
			, "A command-line script depicted some ASCII-art in the standard output, what distracted other processes. The art was sent to a museum and the script was prohibited to use the standard output."
			, "A passive-aggressive thread blocked a processing chain because it was not allowed to process blockchain blocks. The Thread was blocked and the chained processes were processing again."
			, "7 hyperactive methods consumed 0.1% of the CPU. They were in a hyperactive state of waiting. The methods were put into the bin for biodegradable code and replaced with functional methods."
			, "Three factions of disk spaces had formed and a war broke out to decide which one could compress data into less space. The war was stopped by taking their data away and move it to the cloud."
			, "The issue was caused when a server was invited to the cloud. It was absent for a short time to visit the cloud, but it came back right away as our racks are much cozier."
			, "An armada of requests has hit the API and broke some holes into the interface. The interface was fixed by a plumber with highly-elastic bubblegum."
			, "An application user had pressed the right key at the right time. The application got creative and processed a right mouse click 5 minutes in the future instead. This will never occur again."
			, "The firewall changed its model of operation from autonomous to democratic. Before any request on any port gets allowed or blocked, all processes had to vote. The firewall was fired and replaced by a new one."
			, "Virtual crows have started to make their nests in the system. We have installed scarecrow software as a countermeasure. Issue was resolved by moving the nests to the recycle bin."
			, "Your program code tried to break out of it's run directory. It was caught by the operating system and was put back in its place."
			, "A byte-eating bacterium was affecting your machines. It was analyzed and erradicated by the machines immune system."
			, "A group of bytes turned aggressive and assailed some configuration files. The mob was suppressed by a gigabyte of energy drink images, what restored the systems functionality."
			, "An XML-File disguised itself as a JSON-File and snuck into places it doesn't belong. The file was arrested and sentenced to 5 years in the recycle bin."
	
	};
	
	/*4 Random integer between 0 and 9999 generated at startup. Useful to make sure content is reloaded after startup.*/
	public static final int STARTUP_RANDOM_INT = randomFromZeroToInteger(9999);
	
	/*4 Random alphanumerical characters generated at startup. Useful to make sure content is reloaded after startup.*/
	public static final String STARTUP_RANDOM_ALPHANUM = randomStringAlphaNumerical(4);
	
	
	public static Random getInstance() { return random;}
	
	public static Boolean randomBoolean() { return randomBoolean(0);}
	
	public static Boolean randomBoolean(int nullRatioPercent) { 

		if( checkReturnNull(nullRatioPercent) ) { return null; }
		return random.nextInt(100) > 50 ? true : false; 
	}
	
	public static <T> T randomFromArray(T[] array) {
	    return randomFromArray(0, array);
	}
	
	
	private static boolean checkReturnNull(int nullRatioPercent) {
		
		if(nullRatioPercent >= randomIntegerInRange(1, 100) ) {
			return true;
		}
		
		return false;
	}
	
	/******************************************************************************
	 * Returns a random item from an array.
	 * 
	 * @param nullRatioPercent number from 0 to 100 to determine if a null value
	 * should be returned.
	 * 
	 ******************************************************************************/
	public static <T> T randomFromArray(int nullRatioPercent, T[] array) {
		
		if( checkReturnNull(nullRatioPercent) ) { return null; }
		
		int index = random.nextInt(array.length);
		return array[index];
	}
	
	public static String randomFromArray(String[] array) {
	    int index = random.nextInt(array.length);
	    return array[index];
	}
	
	
	public static String randomFirstnameOfGod() { return randomFirstnameOfGod(0); }
	public static String randomLastnameSweden() { return randomLastnameSweden(0); }
	public static String randomMythicalLocation() { return randomMythicalLocation(0); }
	public static String randomExaggaratingAdjective() { return randomExaggaratingAdjective(0); }
	public static String randomIssueResolvedMessage() { return randomIssueResolvedMessage(0); }
	public static String randomMessageOfObedience() { return randomMessageOfObedience(0); }
	
	
	public static String randomFirstnameOfGod(int nullRatioPercent) { return randomFromArray(nullRatioPercent, firstnameGods); }
	public static String randomLastnameSweden(int nullRatioPercent) { return randomFromArray(nullRatioPercent, lastnameSweden); }
	public static String randomMythicalLocation(int nullRatioPercent) { return randomFromArray(nullRatioPercent, mythicalLocations); }
	public static String randomExaggaratingAdjective(int nullRatioPercent) { return randomFromArray(nullRatioPercent, exaggeratingAdjectives); }
	public static String randomIssueResolvedMessage(int nullRatioPercent) { return randomFromArray(nullRatioPercent, issueResolvedMessages); }
	public static String randomMessageOfObedience(int nullRatioPercent) { return randomFromArray(nullRatioPercent, messagesOfObedience); }
	
	
	/******************************************************************************
	 * Creates a random Message Type.
	 ******************************************************************************/
	public static MessageType randomMessageType() { 
		return randomFromArray(MessageType.values());
	}
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
		
		return randomJSONObjectMightyPerson(0, false);
		
	}
	/******************************************************************************
	 * Creates a random json array of people with various properties.
	 * 
	 * @param count
	 ******************************************************************************/
	public static JsonObject randomJSONObjectMightyPerson(int nullRatioPercent, boolean addChartData) { 
		long currentTime = new Date().getTime();
		
		JsonObject object = new JsonObject();
		
		String id = UUID.randomUUID().toString().substring(0, 22);
		object.addProperty("ID",  id);
		object.addProperty("FIRSTNAME", CFW.Random.randomFirstnameOfGod());
		object.addProperty("LASTNAME", CFW.Random.randomLastnameSweden(nullRatioPercent));
		object.addProperty("LOCATION", CFW.Random.randomMythicalLocation(nullRatioPercent));

		object.addProperty("LIKES_TIRAMISU", CFW.Random.randomBoolean(nullRatioPercent));
		object.addProperty("LAST_LOGIN", currentTime-(CFW.Random.randomIntegerInRange(100, 10000)*1000000) );
		object.addProperty("URL", "http://www.example.url/mightyperson?id="+id);
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
