package com.xresch.cfw.utils;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWMessages.MessageType;
import com.xresch.cfw.utils.CFWTime.CFWTimeUnit;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2020 
 **************************************************************************************************************/
public class CFWRandom {

	private static final CFWRandom INSTANCE = new CFWRandom();
	
	private static final Random random = new Random();
	
	public static final String ALPHAS 	  = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
	public static final String ALPHA_NUMS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ12345678901234567890";
	public static final String ALPHA_NUMS_SPECIALS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ12345678901234567890+*%&/()=?!{}[]><:;.,-_+*%&/()=?!{}[]><:;.,-_";
		
	private static final String[] firstnameGods = new String[] {"Zeus", "Hera", "Poseidon", "Cronus", "Aphrodite", "Hades", "Hephaestus", "Apollo", "Athena", "Artemis", "Ares", "Hermes", "Dionysus", "Persephone", "Eros", "Gaia", "Hypnos", "Rhea", "Uranus", "Nike", "Eos", "Pan", "Selene", "Helios", "Heracles", "Odysseus", "Jupiter", "Juno", "Neptune", "Saturn", "Venus", "Pluto", "Vulcan", "Ceres", "Apollo", "Minerva", "Diana", "Mars", "Mercury", "Bacchus", "Proserpine", "Cupid", "Terra", "Somnus", "Ops", "Uranus", "Victoria", "Aurora", "Faunus", "Luna", "Sol", "Hercules", "Ulysses"};
	private static final String[] lastnameSweden = new String[] {"Andersson", "Johansson", "Karlsson", "Nilsson", "Eriksson", "Larsson", "Olsson", "Persson", "Svensson", "Gustafsson", "Pettersson", "Jonsson", "Jansson", "Hansson", "Bengtsson", "Joensson", "Lindberg", "Jakobsson", "Magnusson", "Olofsson", "Lindstroem", "Lindqvist", "Lindgren", "Axelsson", "Berg", "Bergstroem", "Lundberg", "Lind", "Lundgren", "Lundqvist", "Mattsson", "Berglund", "Fredriksson", "Sandberg", "Henriksson", "Forsberg", "Sjoeberg", "Wallin", "Engstroem", "Eklund", "Danielsson", "Lundin", "Hakansson", "Bjoerk", "Bergman", "Gunnarsson", "Holm", "Wikstroem", "Samuelsson", "Isaksson", "Fransson", "Bergqvist", "Nystroem", "Holmberg", "Arvidsson", "Loefgren", "Soederberg", "Nyberg", "Blomqvist", "Claesson", "Nordstroem", "Martensson", "Lundstroem", "Viklund", "Bjoerklund", "Eliasson"};
	private static final String[] mythicalLocations = new String[] {"Agartha", "Alfheim", "Alomkik", "Annwn", "Amaravati", "Arcadia", "Asgard", "Asphodel Meadows", "Atlantis", "Avalon", "Axis Mundi", "Ayotha Amirtha Gangai", "Aztlan", "Baltia", "Biarmaland", "Biringan City", "Brahmapura", "Brittia", "Camelot", "City of the Caesars", "Cloud cuckoo land", "Cockaigne", "Dinas Affaraon", "Ffaraon", "Diyu", "El Dorado", "Elysian Fields", "Feather Mountain", "Garden of Eden", "Garden of the Hesperides", "Finias", "Hawaiki", "Heaven", "Hell", "Hyperborea", "Irkalla", "Islands of the Blessed", "Jabulqa", "Jambudvīpa", "Jotunheim", "Ketumati", "Kingdom of Reynes", "Kingdom of Saguenay", "Kitezh", "Kolob", "Kunlun Mountain", "Kvenland", "Kyoepelinvuori", "La Ciudad Blanca", "Laestrygon", "Lake Parime", "Land of Manu", "Lemuria", "Lintukoto", "Lyonesse", "Mag Mell", "Meropis", "Mictlan", "Mount Penglai", "Mu", "Muspelheim", "Naraka", "New Jerusalem", "Nibiru", "Niflheim", "Niflhel", "Nirvana", "Norumbega", "Nysa", "Olympus", "Paititi", "Panchaia", "Pangaia", "Pandaemonium", "Pleroma", "Pohjola", "Purgatory", "Quivira", "Cíbola", "Ram Setu", "Samavasarana", "Scholomance", "Sierra de la Plata", "Shambhala", "Shangri-La", "Suddene", "Summerland", "Svarga", "Svartalfaheimr", "Takama-ga-hara", "Tartarus", "Themiscyra", "Thule", "Thuvaraiyam Pathi", "Tir na nag", "Vaikuntha", "Valhalla", "Vanaheimr", "Westernesse", "Xanadu", "Shangdu", "Xibalba", "Yomi", "Ys", "Zarahemla", "Zerzura", "Zion"};
	private static final String[] colorNames = new String[] { "Red", "Orange", "Yellow", "Cyan", "Green", "Blue", "Pink", "Purple", "Gold", "Silver", "Bronze" };
	private static final String[] fruitNames = new String[] { "Apple", "Pear", "Orange", "Banana", "Pineapple", "Watermelon", "Grapefruit", "Papaya", "Mango", "Pomegranate", "Lemon", "Cherry", "Apricot", "Peach", "Strawberry", "Plum"};
	private static final String[] italianDesserts = new String[] { "Tiramisu", "Panna Cotta", "Gelato", "Panettone", "Biscotti", "Bombolone", "Colomba di Pasqua", "Confetti", "Frutta Martorana", "Gianduiotto", "Mustacciuoli", "Nutella", "Pandoro", "Pasticciotto", "Ricciarelli", "Semifreddo", "Sanguinaccio Dolce", "Sfogliatella", "Struffoli", "Tartufo", "Torrone", "Torta alla Monferrina", "Torta Tre Monti", "Taralli", "Uovo sbattuto", "Zabaione", "Zuccotto"};
	private static final String[] exaggeratingAdjectives = new String[] { "utterly arduous", "superfluous", "chocolate-addicted", "super-sneaky", "ultra cuddly", "mega religious", "totally angry", "absolutely arrogant", "totally-at-the-ready", "bat-sh*t-crazy", "bull-headed", "100% confused", "fully-cruel-hearted", "over-demanding", "fiercely loyal", "endlessly flirting", "free-loading", "frisky", "god-mode-greedy", "devil-like hateful", "house-broken", "above hyperactive", "high-end", "idiotic", "infuriating", "awfully insecure", "hilariously maniacal", "ultra narrow-minded", "out-of-control", "rebellious", "self-absorbed", "shaky", "shivering", "slippery", "stubborn", "territorial", "tripping", "twisted", "underhanded", "vengeful", "vile", "yapping", "zippy", "zombie-like" };
	private static final String[] ultimateServiceNames = new String[] { "OmniKoore", "Ultima-X", "Zepress-3000", "Hachijuhachi-88", "Samnizt-V8", "Oversharp V3", "Megalytics v2.9", "Korrasoft", "Softikrom P55", "Bro-Jekt 3.0", "OverApp Z", "Extremia 8008", "TX-ULTRA", "PlusQuam-Defect 8.7", "Expandor Type B", "Webator X2", "TotalKoondrol C55"};
	
	private static final String[] jobTitleAdjective = new String[] { "Chief", "Accredited", "Associate", "Lead", "Head", "Overlord", "King", "Serving", "Master", "Executive", "Administrative", "Principal", "President", "Senior", "Junior", "Official", "Unofficial", "Commanding", "Prime", "Sovereign", "Majestic", "Apocalyptic", "Magical", "Hyperactive", "Over-Emotional", "Flirting", "Vanishing", "Running", "Professional", "Academic", "Lost", "Aspiring", "Global", "Vital", "Digital", "Representative", "Organized", "Empowering", "Logical", "Paranoid", "Lunatic", "Mental", "Trustworthy", "Interim", "Emergency", "Online", "Captain", "Boss", "Creative", "Certified", "Judging", "Clueless", "Transparent", "Active", "Neglected", "Satisfied", "Hungry", "Impervious", "Causative", "Effective", "Abstract", "Invasive" };
	private static final String[] jobTitleTopic = new String[] { "Performance", "Infrastructure", "Facility", "Communication", "Executive", "Establishment", "Machine", "Cooperation", "Quality", "Tiramisu", "Technology", "Nutrition", "Treasury", "Dissemination", "Promotion", "Research", "Discovery", "Management", "Area", "Underground", "Heaven", "Hell", "Paradise", "Underworld", "Cleaning", "Screaming", "Information", "Marketing", "Business", "Sales", "Inspiration", "Operations", "Happiness", "Production", "Organization", "Computer", "Unicorn", "Food", "Health", "Workaround", "Coping", "Translation", "Catastrophe", "Security", "Software", "Conversation", "Media", "Internet", "Amazement", "Cheerleader", "Happiness", "Revenue", "Money", "Cream", "Dream", "Steam", "Realm", "Arts", "Crafts", "Army", "Troop"};
	private static final String[] jobTitleRole = new String[] { "Secretary", "Director", "Officer", "Deputy", "In-Charge", "Manager", "Expert", "Engineer", "Executive", "Architect", "Communicator", "Observer", "Neglector", "Slave", "Expeditor", "Locator", "Searcher", "Researcher", "Enabler", "Administrator", "Commander", "Leader", "Minister", "Ambassador", "Magician", "Accelerator", "Soldier", "Wizard", "Cleaner", "Creator", "Maker", "Slacker", "Idler", "Loafer", "Cruncher", "God", "Demi-God", "Editor", "Destroyer", "Collector", "Educator", "Representative", "Handshaker", "Hero", "Generalist", "Genius", "Organizator", "Whisperer", "Emperor", "Registrar", "Supervisor", "Ninja", "Inventor", "Listener", "Troublemaker", "Maestro", "Virtuoso", "Alchemist", "Designer", "Technician", "Commander", "Practitioner" };

	
	private static final String[] statisticsTitleAdjective = new String[] { "Heavy", "Low", "Lovely", "Tasty", "Strong", "Weak", "Enslaved", "Holy", "Spiritual", "Technical", "Digital", "Online", "Virtual", "Real", "Invented", "Shocked", "Cute", "Perfect", "Arduous", "Magical", "Annoying", "Beautiful", "Enraged", "Fallen", "Married", "Dissappointed", "Lost", "Fierce", "Idle", "Heavenly", "Hellish", "Horrifying" };
	private static final String[] statisticsTitleObject = new String[] { "Letters", "People", "Apples", "Tiramisus", "Pillows", "Pens", "Contracts", "Customers", "Patients", "Chairs", "Castles", "Dragons", "Fairies", "Gnomes", "Chemicals", "Elves", "Horses", "Magicians", "Spirits", "Trees", "Books", "Backpacks", "Angels", "Gods", "Devils", "Demons", "Heros", "Soldiers", "Papers", "Games", "Rooms", "Liquids", "Materials" };
	private static final String[] statisticsTitleVerb = new String[] { "Sent", "Written", "Glued", "Woreshipped", "Banned", "Cuddled", "Destroyed", "Hidden", "Stolen", "Horrified", "Made", "Created", "Impressed", "Fortified", "Mixed", "Hunted", "Befriended", "Educated", "Bewitched", "Cursed", "Enchanted", "Backed", "Sold", "Combusted", "Briefed", "Registered", "Educated", "Eradicated", "Engaged", "Divorced", "Played", "Tested", "Documented" };
	
	private static final String[] methodNamePrefix = new String[] {"get", "set", "create", "read", "update", "delete", "execute", "call", "generate", "make", "rename", "add", "remove", "copy", "duplicate", "put", "push", "check", "is", "has", "can", "format", "convert", "send", "poll", "sort", "filter", "decode", "encode", "compress", "decompress", "zip", "unzip", "encrypt", "decrypt", "calculate", "calc", "give", "gimme", "hide", "reveal", "regurgitate", "accrue", "iterate", "write", "recreate", "permit", "levitate", "enchant", "bewitch", "slay", "cast", "refer", "hunt", "find", "conquer", "imperialize", "holify", "deify", "revere", "arise", "awaken", "enshroud", "encroach", "trap", "regurgitate", "master", "train", "enact", "exhibit", "engulf", "overthrow", "acquire", "steal", "shoot", "empower", "embolden", "heal", "revive", "zombify", "persuade"};
	private static final String[] methodNameItem = new String[] {"Customer", "Account", "Table", "Method", "Statistics", "Cell", "Maximum", "Message", "Record", "Category", "Tiramisu", "Source", "Box", "Package", "Graph", "Angel", "Demon", "Perfect", "Digital", "Virtual", "Artificial", "Actual", "First", "Last", "Literal", "Parameter", "Clock", "Berserk", "Magic", "Comment", "Page", "Test", "Money", "Cash", "Energy", "Wave", "Billing", "Cart", "Shop", "Image", "Game", "Taste", "Interest", "Gravity", "Heaven", "Scrumptious", "Royal", "Noble", "Crossbow", "Gunpowder", "Marvelous", "Extraordinary", "Fantastabulous", "Spiffy", "Stealth", "Corroded", "Corrupted", "Deteriorated", "Immoral", "Sabotaged", "Liquified"};
	private static final String[] methodNameSuffix = new String[] {"Count", "Value", "Time", "Role", "Permissions", "Average", "Globals", "Metadata", "Defaults", "Height", "Width", "Dimensions", "Length", "Size", "Activity", "Copy", "Chart", "Extremes", "Overlaps", "Caches", "Rights", "Interval", "Set", "Map", "List", "Collection", "Array", "Object", "Results", "Number", "Status", "Percentage", "Consumption", "Balance", "Time", "Entry", "Row", "Column", "Performance", "Load", "Rate", "Allowance", "Beast", "Angel", "Smithereens", "Priestess", "Sorceress", "Kingdom", "Fishes", "Stars", "Furnance", "Sword", "Spear", "Assassin", "Mage", "Huntress", "Fisherman", "Duchess", "Lord", "King", "Prince", "Waitress", "Enemies", "Sirens", "Overlords", "Overlady", "Empire", "Carpenter", "Phoenix", "Blacksmith", "Zombies", "Ghouls", "Kobolds", "Endeavours" };

	private static final String[] companyTitleFirst = new String[] { 
			  "Rockford"
			, "Battlefield" 
			, "First" 
			, "Second" 
			, "Ultimate" 
			, "Omega" 
			, "Best" 
			, "Expensive" 
			, "Luxus" 
			, "Traders" 
			, "More" 
			, "Over" 
			, "Super" 
			, "Allyn" 
			, "O'Sullivan" 
			, "O'Brian" 
			, "O'Connor" 
			, "O'Deorian" 
			, "McGregor" 
			, "Sister's" 
			, "Brother's" 
			, "Mother's" 
			, "Father's" 
			, "My" 
			, "Expert" 
			, "Cheap" 
			, "Heavy" 
			, "Virtual" 
			, "Internet" 
			, "War" 
			, "Real" 
			, "Unreal" 
			, "Fairy" 
			, "Dragon" 
			, "Dungeon" 
			, "Slave" 
			, "Master" 
			, "Elves" 
			, "Goblin" 
			, "Wyvern" 
			, "Centaur" 
			, "Minotaur" 
			, "Dwarven" 
			, "Custom" 
			, "Lamia" 
			, "Pixie" 
			, "Demon" 
			, "Angel" 
			, "Rocker" 
			, "Magician" 
			, "Knight" 
			, "Sorceress" 
			, "Lizardman" 
			, "Mermaid" 
			, "Zeus" 
			, "Goddess" 
			, "Mythical" 
			, "Magical" 
			, "Fantastic" 
			};
	
	private static final String[] companyTitleSecond = new String[] { 
			  "Wood"
			, "Plastics"
			, "Metals"
			, "Silver"
			, "Gold"
			, "Platinum"
			, "Ceramics"
			, "Fun"
			, "Brothers"
			, "Sisters"
			, "and Family"
			, "and Daughters" 
			, "and Sons" 
			, "and Mothers" 
			, "and Father's" 
			, "IT"
			, "Digital" 
			, "Backery" 
			, "Industry" 
			, "Supermarket" 
			, "Trading" 
			, "Finance" 
			, "Army" 
			, "Weapons" 
			, "Games" 
			, "Gaming" 
			, "Packaging" 
			, "Technology" 
			, "Care" 
			, "Health" 
			, "Computer" 
			, "Specialist" 
			, "Printing" 
			, "3D" 
			, "Stealth" 
			, "Helicopter" 
			, "Aircraft" 
			, "Archeology" 
			, "Astronomy" 
			, "Geology" 
			, "Mathematics" 
			, "Language" 
			, "Housing" 
			, "Fabrics" 
			, "Clothing" 
			, "Underwear" 
			, "Toys" 
			, "Insanity" 
			, "Estate" 
			, "Lumber" 
			, "Meat" 
			, "Fruit" 
			, "Shepherd" 
			, "Dogs" 
			, "Cats" 
			, "Parrots" 
			, "Animals" 
			, "Lamas" 
			, "Elefants" 
			, "Restaurant" 
			, "Take-Away" 
			, "Food" 
			, "Marriage" 
			, "Stories" 
			, "City" 
			, "Tools" 
			, "Crafting" 
			, "Dirt" 
			, "Cleaning" 
			, "Dust" 
			, "Soil" 
			, "Fertilizer" 
			, "Litterbox" 
			, "Security"
			, "Guard"
			, "Floor"
			, "Cooking"
			, "Extermination"
			, "Book"
			};
	
	private static final String[] companyTitleThird = new String[] { 
			  "AG"
			, "GmbH"
			, "Inc."
			, "Ltd."
			, "LCC"
			, "PLCC"
			, "Corp."
			, "Co-Op"
			, "S.A."
			, "S.L."
			, "Business"
			, "Company"
			, "Group"
			, "Corporation"
			, "Services"
			, "Shop"
			, "Store"
			, "School"
			, "University"
			, "Counselor"
			, "Solicitors"
			, "Trader"
			, "Industry"
			, "Industries"
			, "Agency"
			};
	
	private static final String[] firstWorldProblemTitles = 
			new String[] {
				  "User cannot start Session - Cannot find Login Page"
				, "!!! URGENT !!! Antivirus got infected by Virus"
				, "Chat Issue - People do not reply"
				, "User tries to modify an object without a Browser"
				, "Request for Replacement: Computer combusted while camping"
				, "Cannot connect to Netflix from Office Network"
				, "Need exterminator: Mice and cockroaches in Room 36"
				, "COMPLAINT! Friendly Reminders are NOT friendly!!!"
				, "New Employee refuses too use Windows"
				, "Superstitious User wants IDs containing '666' banned"
				, "User u47654 cannot install private Mandala application"
				, "Hardware issue: Monitors to far apart, request assistance"
				, "Can't find letter A on keyboard"
				, "Leakage Issue: Used DVD tray as coffee cup holder"
				, "Co-Worker replied on mail he didn't receive it, what now?"
				, "Cannot receive eMails from Hamburg, normal eMails work!"
				, "Chat Messages get lost after about 700 miles on network"
				, "Don't know how to do my job, please help"
				, "Mouse does not connect when switch is in 'OFF' position"
				, "Nobody showing up when I press F1(help button)"
				, "Made copy of floppy disk, where to store the photocopy?"
				, "Can’t remember email password. Please email new password."
				, "Made copy of floppy disk, where to store the photocopy?"
				, "Cannot boot computer during power outage"
				, "Unplugging/replugging all cables for reboot, easier way?"
				, "User requests to be rolled back to Google from Chrome"
				, "myspacebardoesnotwork-iwouldlikesomespacessoicanwork!"
				, "Please do the needful"
				, "Help! My internet is shrinking!"
				, "Terrorists entered my computer"
				, "Help! I deleted the internet!"
				, "Domain has trust issue - how to get more trust"
				, "Coffee machine not working, team about to die"
				, "When typing password only stars are filled in"
				, "Can you reboot the Internet? It seems to be pretty slow."
				, "Accepted Cookies but they were never sent to me."
				, "Cannot open Excel file in Word"
				, "Boss said to buy a mouse, where to get food for it?"
				, "Need offline version of the internet"
			};
	
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
	public static final int STARTUP_RANDOM_INT = fromZeroToInteger(9999);
	
	/*4 Random alphanumerical characters generated at startup. Useful to make sure content is reloaded after startup.*/
	public static final String STARTUP_RANDOM_ALPHANUM = stringAlphaNum(4);
	
	/******************************************************************************
	 * Returns an instance of Random.
	 ******************************************************************************/
	public static Random getInstance() { return random; }
	
	/******************************************************************************
	 * 
	 ******************************************************************************/
	public static boolean checkReturnNull(int nullRatioPercent) {
		
		if(nullRatioPercent >= integerInRange(1, 100) ) {
			return true;
		}
		
		return false;
	}
	
	/******************************************************************************
	 * Returns a random Boolean
	 ******************************************************************************/
	public static Boolean bool() { return bool(0);}
	
	/******************************************************************************
	 * Returns a random Boolean and maybe null.
	 ******************************************************************************/
	public static Boolean bool(int nullRatioPercent) { 

		if( checkReturnNull(nullRatioPercent) ) { return null; }
		return random.nextInt(100) > 50 ? true : false; 
	}

	
	/******************************************************************************
	 * Returns a random item from Set.
	 * 
	 * @param nullRatioPercent number from 0 to 100 to determine if a null value
	 * should be returned.
	 * @param set to choose from
	 * 
	 * @return random value, null if Set is empty or null
	 * 
	 ******************************************************************************/
	public static <T> T fromSet(int nullRatioPercent, Set<T> set) {
		
		if(set == null || set.isEmpty()) {
			return null;
		}
		
		if( checkReturnNull(nullRatioPercent) ) { return null; }
		
		
		int index = random.nextInt(set.size());

		int counter = 0;
		
		T result = null ;
		for(T element : set) {
			if(counter >= index) {
				result = element;
				break;
			}
			counter++;
		}
		
		return result;
		
	}
	
	/******************************************************************************
	 * Returns a random String from an array.
	 * 
	 * @param array to choose from
	 * 
	 ******************************************************************************/
	public static String fromArray(String[] array) {
	    int index = random.nextInt(array.length);
	    return array[index];
	}
	
	/******************************************************************************
	 * Returns a random value from the given array.
	 ******************************************************************************/
	public static <T> T fromArray(T[] array) {
	    return fromArray(0, array);
	}
	


	/******************************************************************************
	 * Returns a random item from an array.
	 * 
	 * @param nullRatioPercent number from 0 to 100 to determine if a null value
	 * should be returned.
	 * 
	 ******************************************************************************/
	public static <T> T fromArray(int nullRatioPercent, T[] array) {
		
		if( checkReturnNull(nullRatioPercent) ) { return null; }
		
		int index = random.nextInt(array.length);
		return array[index];
	}
	

	
	//==============================================================================
	// Various methods calling randomFromArray
	//==============================================================================
	public static String firstnameOfGod(int nullRatioPercent) { return fromArray(nullRatioPercent, firstnameGods); }
	public static String lastnameSweden(int nullRatioPercent) { return fromArray(nullRatioPercent, lastnameSweden); }
	public static String mythicalLocation(int nullRatioPercent) { return fromArray(nullRatioPercent, mythicalLocations); }
	public static String ultimateServiceName(int nullRatioPercent) { return fromArray(nullRatioPercent, ultimateServiceNames); }
	public static String colorName(int nullRatioPercent) { return fromArray(nullRatioPercent, colorNames); }
	public static String fruitName(int nullRatioPercent) { return fromArray(nullRatioPercent, fruitNames); }
	public static String italianDessert(int nullRatioPercent) { return fromArray(nullRatioPercent, italianDesserts); }
	public static String exaggaratingAdjective(int nullRatioPercent) { return fromArray(nullRatioPercent, exaggeratingAdjectives); }
	public static String issueResolvedMessage(int nullRatioPercent) { return fromArray(nullRatioPercent, issueResolvedMessages); }
	public static String messageOfObedience(int nullRatioPercent) { return fromArray(nullRatioPercent, messagesOfObedience); }
	
	public static String jobTitle(int nullRatioPercent) { 
		
		if( checkReturnNull(nullRatioPercent) ) { return null; }
		
		return fromArray(jobTitleAdjective)
		+ " " +fromArray(jobTitleTopic)
		+ " " +fromArray(jobTitleRole)
			; 
	}
	
	public static String statisticsTitle(int nullRatioPercent) { 
		
		if( checkReturnNull(nullRatioPercent) ) { return null; }
		
		return fromArray(statisticsTitleAdjective)
		+ " " +fromArray(statisticsTitleObject)
		+ " " +fromArray(statisticsTitleVerb)
			; 
	}
	
	public static String methodName(int nullRatioPercent) { 
		
		if( checkReturnNull(nullRatioPercent) ) { return null; }
		
		return fromArray(methodNamePrefix)
		+fromArray(methodNameItem)
		+fromArray(methodNameSuffix)
		+ "()"
			; 
	}
	
	public static String companyTitle(int nullRatioPercent) { 
		
		if( checkReturnNull(nullRatioPercent) ) { return null; }
		
		return fromArray(companyTitleFirst)
		+ " " +fromArray(companyTitleSecond)
		+ " " +fromArray(companyTitleThird)
			; 
	}
	
	
	//==============================================================================
	// Overload methonds for above with 0% null ratio
	//==============================================================================
	public static String firstnameOfGod() { return firstnameOfGod(0); }
	public static String lastnameSweden() { return lastnameSweden(0); }
	public static String mythicalLocation() { return mythicalLocation(0); }
	public static String ultimateServiceName() { return ultimateServiceName(0); }
	public static String colorName() { return colorName(0); }
	public static String fruitName() { return fruitName(0); }
	public static String italianDessert() { return italianDessert(0); }
	public static String exaggaratingAdjective() { return exaggaratingAdjective(0); }
	public static String issueResolvedMessage() { return issueResolvedMessage(0); }
	public static String messageOfObedience() { return messageOfObedience(0); }
	public static String jobTitle() { return jobTitle(0); }
	public static String statisticsTitle() { return statisticsTitle(0); }
	public static String methodName() { return methodName(0); }
	public static String companyTitle() { return companyTitle(0); }

	/******************************************************************************
	 * Creates a random Message Type.
	 ******************************************************************************/
	public static MessageType messageType() { 
		return fromArray(MessageType.values());
	}
	/******************************************************************************
	 * Creates a random json array of people with various properties.
	 * 
	 * @param count
	 ******************************************************************************/
	public static String[] arrayOfExaggaratingAdjectives(int count) { 
		
		String[] stringArray = new String[count];
			
		for(int i = 0; i < count; i++) {
			stringArray[i] = exaggaratingAdjective();
		}
		
		return stringArray;
	}
	
	/******************************************************************************
	 * Creates a random integer between 0(inclusive) and the given number(inclusive).
	 * 
	 ******************************************************************************/
	public static Integer fromZeroToInteger(int upperInclusive) {
		
		return ThreadLocalRandom.current().nextInt(upperInclusive+1);
	}
	
	/******************************************************************************
	 * Creates a random integer between the given numbers(inclusive).
	 * Returns nulls for a certain percentage
	 * 
	 ******************************************************************************/
	public static Integer integerInRange(int lowerInclusive, int upperInclusive, int nullRatioPercent) {
		if( checkReturnNull(nullRatioPercent) ) { return null; }
		
		return integerInRange(lowerInclusive, upperInclusive);
	}
	/******************************************************************************
	 * Creates a random integer between the given numbers(inclusive).
	 * 
	 ******************************************************************************/
	public static Integer integerInRange(int lowerInclusive, int upperInclusive) {
		
		return ThreadLocalRandom.current().nextInt(lowerInclusive, upperInclusive+1);
	}
	
	/******************************************************************************
	 * Creates a random long between the given numbers(inclusive).
	 * 
	 ******************************************************************************/
	public static Long longInRange(long lowerInclusive, long upperInclusive) {
		return ThreadLocalRandom.current().nextLong(lowerInclusive, upperInclusive+1);
	}
	
	/******************************************************************************
	 * Creates a random double between the given numbers(inclusive).
	 * 
	 ******************************************************************************/
	public static Double doubleInRange(double lowerInclusive, double upperInclusive) {
		
		return ThreadLocalRandom.current().nextDouble(lowerInclusive, upperInclusive+1);
	}
	
	/******************************************************************************
	 * Creates a random float between the given numbers(inclusive).
	 * 
	 ******************************************************************************/
	public static Float floatInRange(float lowerInclusive, float upperInclusive, int nullRatioPercent) {
		
		if( checkReturnNull(nullRatioPercent) ) { return null; }
		
		return floatInRange(lowerInclusive, upperInclusive);
	}
	
	/******************************************************************************
	 * Creates a random float between the given numbers(inclusive).
	 * 
	 ******************************************************************************/
	public static Float floatInRange(float lowerInclusive, float upperInclusive) {
		
		float randomFloat = ThreadLocalRandom.current().nextFloat();
		float diff = upperInclusive - lowerInclusive;
		return (randomFloat * diff) + lowerInclusive;
		
	}
	
	/******************************************************************************
	 * Creates a random BigDecimal based on long number, no fractions.
	 * 
	 * @return
	 ******************************************************************************/
	public static BigDecimal bigDecimal(long lowerInclusive, long upperInclusive) {
		
		long number = CFW.Random.longInRange(lowerInclusive, upperInclusive);
		return new BigDecimal(number);

	}
	
	/******************************************************************************
	 * Creates a random BigDecimal based on int number, no fractions.
	 * 
	 * @return
	 ******************************************************************************/
	public static BigDecimal bigDecimal(int lowerInclusive, int upperInclusive, int maxDecimals) {
		return bigDecimal((long)lowerInclusive, (long)upperInclusive, maxDecimals);
	}
	
	/******************************************************************************
	 * Creates a random BigDecimal based on long number, no fractions.
	 * 
	 * @return
	 ******************************************************************************/
	public static BigDecimal bigDecimal(long lowerInclusive, long upperInclusive, int maxDecimals) {
		
		long number = CFW.Random.longInRange(lowerInclusive, upperInclusive);
		
		
		long decimals = CFW.Random.longInRange(1, 9 * (10 ^ (maxDecimals-1)));
		
		BigDecimal decimal = new BigDecimal(number+"."+decimals);
		
		return decimal ;
	}
	
	
	/******************************************************************************
	 * Creates a random Timestamp between given milliseconds(inclusive).
	 * 
	 ******************************************************************************/
	public static Timestamp timestamp(Timestamp earliestInclusive, Timestamp latestInclusive) {
		
		return timestamp(earliestInclusive.getTime(), latestInclusive.getTime());
	}
	
	/******************************************************************************
	 * Creates a random Timestamp between given milliseconds(inclusive).
	 * Example Usage:
	 * <pre><code>
	   CFW.Random.randomTimestampInRange(
			CFWTimeUnit.d.offset(null, -30), 
			CFWTimeUnit.m.offset(null, -30)
		);
		</code></pre>
	 * 
	 ******************************************************************************/
	public static Timestamp timestamp(long earliestInclusive, long latestInclusive) {
		
		long timeMillis = longInRange(earliestInclusive, latestInclusive);
		return new Timestamp(timeMillis);
	}
		
	
	/******************************************************************************
	 * Creates a random String containing lower and uppercase characters.
	 * 
	 * @param byteCount number of bytes to create
	 * @return
	 ******************************************************************************/
	public static String string(int byteCount) {
	
		StringBuilder builder = new StringBuilder();
	
		//Random random = getInstance();
		for (int i = 0; i < byteCount; i++) {
			builder.append(ALPHAS.charAt(random.nextInt(51)));
		}
	
		return builder.toString();
	
	}

	/******************************************************************************
	 * Creates a random String containing alphanumerical characters.
	 * 
	 * @param byteCount number of bytes to create
	 * @return
	 ******************************************************************************/
	public static String stringAlphaNum(int byteCount) {
	
		StringBuilder builder = new StringBuilder();
	
		//Random random = getInstance();
		for (int i = 0; i < byteCount; i++) {
			builder.append(ALPHA_NUMS.charAt(random.nextInt(51)));
		}
	
		return builder.toString();
	
	}
	/******************************************************************************
	 * Creates a random String containing alphanumerical and special characters.
	 * 
	 * @param byteCount number of bytes to create
	 * @return
	 ******************************************************************************/
	public static String stringAlphaNumSpecial(int byteCount) {
	
		StringBuilder builder = new StringBuilder();
	
		//Random random = getInstance();
		for (int i = 0; i < byteCount; i++) {
			builder.append(ALPHA_NUMS_SPECIALS.charAt(random.nextInt(51)));
		}
	
		return builder.toString();
	
	}
	
	/************************************************************************************************
	 * Creates a random HSL CSS string like "hsla(112, 54, 210, 1.0)".
	 * @param minS The minimum saturation in percent 0-100
	 * @param maxS The maximum saturation in percent 0-100
	 * @param minL The minimum Lightness in percent 0-100
	 * @param maxL The maximum Lightness in percent 0-100
	 ************************************************************************************************/
	public static String colorHSL(int minS, int maxS, int minL, int maxL) { 
		
		int h = integerInRange(0,256);
		int s = integerInRange(minS, maxS);
		int l = integerInRange(minL, maxL);
		
		return "hsla("+h+","+s+"%,"+l+"%, 1.0)";
		
	}
	
	/************************************************************************************************
	 * Creates a random HSL CSS string like "hsla(112, 54, 210, 1.0)".
	 * @param hue The number that should be used for the hue.
	 * @param minS The minimum saturation in percent 0-100
	 * @param maxS The maximum saturation in percent 0-100
	 * @param minL The minimum Lightness in percent 0-100
	 * @param maxL The maximum Lightness in percent 0-100
	 ************************************************************************************************/
	public static String colorSL(int hue, int minS, int maxS, int minL, int maxL) { 
		
		int s = integerInRange(minS, maxS);
		int l = integerInRange(minL, maxL);
		
		return "hsla("+Math.abs(hue % 360)+","+s+"%,"+l+"%, 1.0)";
		
	}
	
	/******************************************************************************
	 * Enum of data types used in the next method.
	 * 
	 ******************************************************************************/
	public enum RandomDataType {
		  DEFAULT 
		, NUMBERS
		, ARRAYS
		, SERIES
		, STATS
		, TRADING
		, TICKETS
		, BATCHJOBS
		, VARIOUS
		;
		
		private static HashSet<String> names = new HashSet<>();
		static {
			for(RandomDataType type : RandomDataType.values()) {
				names.add(type.name());
			}
		}

		public static boolean has(String value) {
			return names.contains(value);
		}
	}
	
	/******************************************************************************
	 * Creates a random arrayList of integers.
	 * 
	 * @param count
	 ******************************************************************************/
	public static ArrayList<Integer> arrayListOfIntegers(int count, int lowerInclusive, int upperInclusive) { 
		ArrayList<Integer> array = new ArrayList<Integer>();
				
		for(int i = 0; i < count; i++) {
			array.add(integerInRange(lowerInclusive, upperInclusive));
		}
		
		return array;
	}
	
	/******************************************************************************
	 * Creates a random array list of Swedish Lastnames.
	 * 
	 * @param count
	 ******************************************************************************/
	public static ArrayList<String> arrayListOfSwedishLastnames(int count) { 
		ArrayList<String> array = new ArrayList<>();
		
		for(int i = 0; i < count; i++) {
			array.add(lastnameSweden());
		}
		
		return array;
	}
	
	/******************************************************************************
	 * Creates a random list of records of a specific type of data.
	 * 
	 * @param count number of records to generate
	 * @param type the type of data to generate
	 * @param seriesCount count of series, ignored if the data type has no series 
	 * @param earliest time to generate (epoch millis)
	 * @param earliest time to generate (epoch millis)
	 * 
	 ******************************************************************************/
	public static JsonArray records(
			  int count
			, RandomDataType type
			, int seriesCount
			, long earliest
			, long latest
		){

		switch(type) {
		
			case DEFAULT:		return CFW.Random.jsonArrayOfMightyPeople(count, 5, earliest, latest);
			case NUMBERS:		return CFW.Random.jsonArrayOfNumberData(count, 0, earliest, latest);
			case ARRAYS:		return CFW.Random.jsonArrayOfArrayData(count, 0, earliest, latest);
			case SERIES:		return CFW.Random.jsonArrayOfSeriesData(seriesCount, count, earliest, latest);
			case STATS:			return CFW.Random.jsonArrayOfStatisticalSeriesData(seriesCount, count, earliest, latest);
			case TRADING:		return CFW.Random.jsonArrayOfTradingData(seriesCount, count, earliest, latest);
			case TICKETS: 		return CFW.Random.jsonArrayOfSupportTickets(count);
			case BATCHJOBS:		return CFW.Random.jsonArrayOfBatchCalls(seriesCount, count, earliest, latest, 7);
			case VARIOUS:		return CFW.Random.jsonArrayOfVariousData(count, 0, earliest, latest);
			
			default: 			return  CFW.Random.jsonArrayOfMightyPeople(count, 5, earliest, latest);
		}

	}
	
	/******************************************************************************
	 * Creates a random json array of people with various properties.
	 * 
	 * @param count
	 ******************************************************************************/
	public static JsonArray jsonArrayOfRandomStrings(int count, int stringLength) { 
		JsonArray array = new JsonArray();
				
		for(int i = 0; i < count; i++) {
			array.add(string(stringLength));
		}
		
		return array;
	}
	
	/******************************************************************************
	 * Creates a random json array.
	 * 
	 * @param count
	 ******************************************************************************/
	public static JsonArray jsonArrayOfMixedTypes(int minElements, int maxElements) { 
		JsonArray array = new JsonArray();
		
		int count = integerInRange(minElements, maxElements);
		for(int i = 0; i < count; i++) {
			
			switch(i % 4) {
				case 0: array.add(mythicalLocation(15));  break;
				case 1: array.add(bool(15));  break;
				case 2: array.add(integerInRange(0, 100));  break;
				case 3: array.add(floatInRange(0, 10000));  break;
				default: array.add(stringAlphaNumSpecial(6));  break;
			}
			
		}
		
		return array;
	}
	
	/******************************************************************************
	 * Creates a random json array of people with various properties.
	 * 
	 * @param count
	 ******************************************************************************/
	public static JsonArray jsonArrayOfCharacters(int count) { 
		JsonArray array = new JsonArray();
				
		for(int i = 0; i < count; i++) {
			array.add(string(1));
		}
		
		return array;
	}
	

	/******************************************************************************
	 * Creates a random json array of people with various properties.
	 * 
	 * @param count
	 ******************************************************************************/
	public static JsonArray jsonArrayOfIntegers(int count, int lowerInclusive, int upperInclusive) { 
		JsonArray array = new JsonArray();
				
		for(int i = 0; i < count; i++) {
			array.add(integerInRange(lowerInclusive, upperInclusive));
		}
		
		return array;
	}
	
	/******************************************************************************
	 * Creates a random json array of people with various properties.
	 * 
	 * @param count
	 ******************************************************************************/
	public static JsonArray jsonArrayOfMightyPeople(int count) { 
		
		long now = System.currentTimeMillis();
		return jsonArrayOfMightyPeople(count,  0, CFWTimeUnit.h.offset(now, -1), now);
	}
	
	
	/******************************************************************************
	 * Creates a random json array of people with various properties.
	 * 
	 * @param count number of records
	 * @param nullRatioPercent how often null values should be added in records
	 * @param earliest time
	 * @param latest time
	 ******************************************************************************/
	public static JsonArray jsonArrayOfMightyPeople(int count, int nullRatioPercent, long earliest, long latest) { 
		JsonArray array = new JsonArray();
		
		long diff = latest - earliest;
		long diffStep = diff / count;
		
		for(int i = 0; i < count; i++) {
			JsonObject person = CFWRandom.jsonObjectMightyPerson(nullRatioPercent);
			person.addProperty("INDEX", i );
			person.addProperty("TIME", earliest +(i * diffStep));
			array.add(person);
		}
		
		return array;
	}
	
	/******************************************************************************
	 * Creates a random json array of records containing number data.
	 * 
	 * @param count number of records
	 * @param nullRatioPercent how often null values should be added in records
	 * @param earliest time
	 * @param latest time
	 ******************************************************************************/
	public static JsonArray jsonArrayOfNumberData(int count, int nullRatioPercent, long earliest, long latest) { 
		JsonArray array = new JsonArray();
		
		long diff = latest - earliest;
		long diffStep = diff / count;
		
		for(int i = 0; i < count; i++) {
			JsonObject object = CFWRandom.jsonObjectNumberData(nullRatioPercent);
			object.addProperty("TIME", earliest +(i * diffStep));
			array.add(object);
		}
		
		return array;
	}
	
	/******************************************************************************
	 * Creates a random json array of records containing array data.
	 * 
	 * @param count number of records
	 * @param nullRatioPercent how often null values should be added in records
	 * @param earliest time
	 * @param latest time
	 ******************************************************************************/
	public static JsonArray jsonArrayOfArrayData(int count, int nullRatioPercent, long earliest, long latest) { 
		JsonArray array = new JsonArray();
		
		long diff = latest - earliest;
		long diffStep = diff / count;
		
		for(int i = 0; i < count; i++) {
			JsonObject object = CFWRandom.jsonObjectArrayData(nullRatioPercent);
			object.addProperty("TIME", earliest +(i * diffStep));
			array.add(object);
		}
		
		return array;
	}
	
	/******************************************************************************
	 * Creates a random json array of records containing various data.
	 * 
	 * @param count number of records
	 * @param nullRatioPercent how often null values should be added in records
	 * @param earliest time
	 * @param latest time
	 ******************************************************************************/
	public static JsonArray jsonArrayOfVariousData(int count, int nullRatioPercent, long earliest, long latest) { 
		JsonArray array = new JsonArray();
		
		long diff = latest - earliest;
		long diffStep = diff / count;
		
		for(int i = 0; i < count; i++) {
			JsonObject object = CFWRandom.jsonObjectVariousData(nullRatioPercent);
			
			JsonObject graphData = new JsonObject();
			graphData.addProperty("x", i+0);
			graphData.addProperty("y", Math.sin(i+1));
			object.add("GRAPH_DATA", graphData);
			
			object.addProperty("TIME", earliest +(i * diffStep));
			
			array.add(object);
		}
		
		return array;
	}
	
	/******************************************************************************
	 * Creates a random json array of support tickets.
	 * 
	 * @param count
	 ******************************************************************************/
	public static JsonArray jsonArrayOfSupportTickets(int count) { 
		JsonArray array = new JsonArray();
				
		for(int i = 0; i < count; i++) {
			array.add(jsonObjectSupportTickets());
		}
		
		return array;
	}
	/******************************************************************************
	 * Creates a random json array of people with various properties.
	 * 
	 * @param valuesCount
	 ******************************************************************************/
	public static JsonArray jsonArrayOfBatchCalls(int seriesCount, int valuesCount, long earliest, long latest, int maxDepth) { 
		JsonArray array = new JsonArray();
		for(int k = 0; k < seriesCount; k++) {
			JsonArray series = new JsonArray();
			jsonArrayOfBatchCalls(series, ultimateServiceName(), valuesCount, earliest, latest, maxDepth);
			array.addAll(series);
		}
		return array;
	}
	
	/******************************************************************************
	 * Creates a random json array of people with various properties.
	 * 
	 * @param valuesCount
	 ******************************************************************************/
	private static JsonArray jsonArrayOfBatchCalls(JsonArray array, String serviceName, int valuesCount, long earliest, long latest, int maxDepth) { 
		
		
		long span = latest - earliest;
		//--------------------------------------
		// Additional Values
		JsonObject currentItem = new JsonObject();
						
		currentItem.addProperty("SERVICE", serviceName);
		currentItem.addProperty("NAME", methodName());
		currentItem.addProperty("QUEUED", earliest - longInRange( 0, (span / 3) ) );
		currentItem.addProperty("START", earliest);
		currentItem.addProperty("END", latest);
		currentItem.addProperty("DURATION", latest - earliest);
		currentItem.addProperty("REPORTED", latest + longInRange( 0 , (span / 3) ) );
		currentItem.addProperty("RESULTS", integerInRange(0,10000) );
		currentItem.addProperty("ERRORS", (integerInRange(0, 100) > 20) 
											? 0 
											: integerInRange(5,1000) 
											);

		if(maxDepth == 0 || array.size() >= valuesCount) {
			return array;
		}
		
		//--------------------------------------
		// Additional Values
		array.add(currentItem);

		//--------------------------------------
		// Create Values for Series
		int directCalls = 0;
		if(maxDepth > 0) {
			directCalls = integerInRange(0, (maxDepth / 2)+1);
		}
		
		// tighten the timeframe
		long oneTwentieth = (latest - earliest) / 20;
		earliest = earliest + longInRange( 0, oneTwentieth );
		latest =  latest + longInRange( 0, oneTwentieth );
		
		long currentEarliest = earliest;
		long remainder = latest - earliest;

		for(int j = 0; j < directCalls; j++) {
			
			long part = remainder / (directCalls-j);
			
			long currentLatest = currentEarliest + longInRange( Math.round(part * 0.75), part);
		
			
			jsonArrayOfBatchCalls(array, serviceName, valuesCount, currentEarliest, currentLatest, maxDepth - 1);
			
			//---------------------------
			// Check Size Limit Reached
			if(array.size() >= valuesCount) {
				return array;
			}
			
			//---------------------------
			// Do Next
			remainder = latest - currentEarliest;
			currentEarliest = currentLatest;
			
		}
		
		return array;
	}
	
	/******************************************************************************
	 * Creates a random json array of people with various properties.
	 * 
	 * @param valuesCount
	 ******************************************************************************/
	public static JsonArray jsonArrayOfStatisticalSeriesData(int seriesCount, int valuesCount, long earliest, long latest) { 
		JsonArray array = new JsonArray();
		
		long timerange = latest - earliest;
		long timestep = timerange / valuesCount;
		
		Integer[] decimals = new Integer[] {1, 2, 3};
		
		//--------------------------------------
		// Create Series 
		for(int i = 0; i < seriesCount; i++) {
			String statisticName = statisticsTitle();

			//--------------------------------------
			// Create Values for Series
			for(int j = 0; j < valuesCount; j++) {
				
				JsonObject currentItem = new JsonObject();
				
				int count = integerInRange(5,100);
				
				ArrayList<BigDecimal> values = new ArrayList<>();
				int lowerBound = integerInRange(0, 20);
				int upperBound = integerInRange(40, 100);
				
				int outlierPercentage = integerInRange(0,100);
				if     (outlierPercentage > 95) { upperBound = integerInRange(200,500); }
				else if(outlierPercentage > 90) { upperBound = integerInRange(100,200); }
					
				for(int k = 0; k < count; k++) {
					values.add( bigDecimal(lowerBound, upperBound, fromArray(decimals) ) );
				}
				currentItem.addProperty("TIME", earliest+(timestep*j));
				currentItem.addProperty("STATISTIC", statisticName);

				currentItem.addProperty("COUNT", count );
				currentItem.addProperty("MIN", CFW.Math.bigMin(values) );
				currentItem.addProperty("AVG", CFW.Math.bigAvg(values, CFW.Math.GLOBAL_SCALE, true) );
				currentItem.addProperty("MAX", CFW.Math.bigMax(values) );
				currentItem.addProperty("SUM", CFW.Math.bigSum(values, CFW.Math.GLOBAL_SCALE, true) );
				currentItem.addProperty("MEDIAN", CFW.Math.bigMedian(values) );
				currentItem.addProperty("STDEV", CFW.Math.bigStdev(values,true, CFW.Math.GLOBAL_SCALE) );
				currentItem.addProperty("P90", CFW.Math.bigPercentile(90, values) );
				currentItem.addProperty("P95", CFW.Math.bigPercentile(95, values) );
				
				//--------------------------------------
				// Additional Values
				array.add(currentItem);
			}
		}
		
		return array;
	}
	
	/******************************************************************************
	 * Creates a random json array of people with various properties.
	 * 
	 * @param valuesCount
	 ******************************************************************************/
	public static JsonArray jsonArrayOfTradingData(int seriesCount, int valuesCount, long earliest, long latest) { 
		JsonArray array = new JsonArray();
		
		long timerange = latest - earliest;
		long timestep = timerange / valuesCount;
		
		//--------------------------------------
		// Create Series 
		for(int i = 0; i < seriesCount; i++) {
			String symbol = string(3).toUpperCase();
			String stockName = companyTitle();
			
			float open = floatInRange(0, fromArray(new Integer[]{1,10, 50, 100, 100, 100, 200, 500, 1000, 3000}) );
			float volatilityPercent = floatInRange(0.01f, 0.1f);
			float lowerVolatility = 1.0f - volatilityPercent;
			float highervolatility = 1.0f + volatilityPercent;
			
			//--------------------------------------
			// Create Values for Series
			for(int j = 0; j < valuesCount; j++) {
				
				JsonObject currentItem = new JsonObject();
				
				float close = floatInRange(open * lowerVolatility, open * highervolatility);
				float high = floatInRange(Math.max(open, close), Math.max(open, close) * 1.1f);
				float low = floatInRange(Math.min(open, close), Math.min(open, close) * 0.9f);
				

				currentItem.addProperty("TIME", earliest+(timestep*j));
				currentItem.addProperty("SYMBOL", symbol);
				currentItem.addProperty("NAME", stockName);
				
				currentItem.addProperty("OPEN",  new BigDecimal(open).setScale(3, CFW.Math.ROUND_UP) );
				currentItem.addProperty("CLOSE", new BigDecimal(close).setScale(3, CFW.Math.ROUND_UP) );
				currentItem.addProperty("HIGH",  new BigDecimal(high).setScale(3, CFW.Math.ROUND_UP) );
				currentItem.addProperty("LOW",   new BigDecimal(low).setScale(3, CFW.Math.ROUND_UP) );

				
				open = close;
				//--------------------------------------
				// Additional Values
				array.add(currentItem);
			}
		}
		
		return array;
	}
	/******************************************************************************
	 * Creates a random json array of people with various properties.
	 * 
	 * @param valuesCount
	 ******************************************************************************/
	public static JsonArray jsonArrayOfSeriesData(int seriesCount, int valuesCount, long earliest, long latest) { 
		JsonArray array = new JsonArray();
		
		long timerange = latest - earliest;
		long timestep = timerange / valuesCount;
		
		String[] classesArray = new String[] {"A", "B", "C", "D", "E", "F", "G"};
		Integer[] boxsizeArray = new Integer[] {6,10,12,16,20,24,36,64,100,144};
		
		//--------------------------------------
		// Create Series 
		for(int i = 0; i < seriesCount; i++) {
			String warehouse = colorName()+" "+stringAlphaNum(1).toUpperCase()+integerInRange(1, 9);
			String item = fruitName();
			float priceMultiplier = floatInRange(0.5f, 5.6f);
						
			//--------------------------------------
			// Create Values for Series
			RandomSeriesGenerator generator = INSTANCE.new RandomSeriesGenerator(valuesCount);
			for(int j = 0; j < valuesCount; j++) {
				JsonObject currentItem = new JsonObject();
				currentItem.addProperty("TIME", earliest+(timestep*j));
				currentItem.addProperty("WAREHOUSE", warehouse);
				currentItem.addProperty("ITEM", item);
				currentItem.addProperty("CLASS", fromArray(classesArray));
				
				currentItem.addProperty("COUNT", generator.getValue(j) );
				
				//--------------------------------------
				// Additional Values
				Float price = floatInRange(0.5f, 1.6f) * priceMultiplier;
				int boxSize = fromArray(boxsizeArray);
				
				currentItem.addProperty("PRICE", price);
				currentItem.addProperty("BOX_SIZE", boxSize);
				currentItem.addProperty("PERCENT", integerInRange(1, 100));
				
				currentItem.addProperty("TOTAL", price * boxSize );
				
				array.add(currentItem);
			}
		}
		
		return array;
	}
	
	/******************************************************************************
	 * Creates random series generator.
	 ******************************************************************************/
	public static RandomSeriesGenerator createRandomSeriesGenerator(int totalValuesCount){
		return INSTANCE.new RandomSeriesGenerator(totalValuesCount);
	}
	
	/******************************************************************************
	 * Creates random values in series.
	 ******************************************************************************/
	public class RandomSeriesGenerator{
		
		private int seriesType = integerInRange(0, 9);
		private int base = integerInRange(0, 100);
		private float jumpPosition1 = floatInRange(0.3f, 5f);
		private float jumpPosition2 = floatInRange(0.3f, 5f);
		private float smallerJump = Math.min(jumpPosition1, jumpPosition2);
		private float biggerJump = Math.max(jumpPosition1, jumpPosition2);
		
		// holds the total amount of values to generate
		int totalValuesCount;
		
		public RandomSeriesGenerator(int totalValuesCount) {
			this.totalValuesCount = totalValuesCount;
		}
		
		
		public Integer getValue(int index) {
			
			switch(seriesType) {
				case 0: // random
					return integerInRange(0, 100);
				
				case 1: // increase
					return (int) ((Math.abs(Math.sin(index)) * 30) + integerInRange(5, 15) * (index / 10.0) );
				
				case 2: // decrease
					float divisor = totalValuesCount / ((totalValuesCount - index) / 1.1f);
					return (int) Math.round( ((Math.abs(Math.sin(index)) * 30) + integerInRange(5, 15)) / divisor) ;
				
				case 3: //jump up
					if((totalValuesCount / (float)(index+1)) > jumpPosition1) {
						return integerInRange(10, 30);
					}else {
						return 70 + integerInRange(0, 30);
					}
				
				case 4: //jump down
					if((totalValuesCount / (float)(index+1)) > jumpPosition2) {
						return integerInRange(60, 100);
					}else {
						return integerInRange(5, 30);
					}
				
				case 5: //jump up & down
					if((totalValuesCount / (float)(index+1)) > biggerJump) {
						return integerInRange(15, 25);
					}else if ( (totalValuesCount / (float)(index+1)) > smallerJump) {
						return 70 + integerInRange(0, 30);
					}else {
						return integerInRange(15, 25);
					}	
	
				case 6: //jump down & up
					if((totalValuesCount / (float)(index+1)) > biggerJump) {
						return integerInRange(70, 90);
					}else if ( (totalValuesCount / (float)(index+1)) > smallerJump) {
						return integerInRange(10, 25);
					}else {
						return integerInRange(70, 90);
					}
				
				case 7: // Sine Wave Increasing
					return (int) Math.round( (base + (Math.sin(index/4.0) * 10)) * (index / 25.0) );
				
				case 8: // Sine Wave Decreasing
					float divisorSine = totalValuesCount / ((totalValuesCount - index) / 1.1f);
					return (int) Math.round( (base + (Math.sin(index/4.0) * 10) + integerInRange(0, 3)) / divisorSine );
				
				case 9: // Sine Random
					return (int) Math.round( base + (Math.abs(Math.sin(index/4.0)) * 10) + integerInRange(0, 5) );

				case 10: // Sinus + Cos Increasing
					return (int) Math.round( (
								(Math.abs(Math.cos((index)/6)) * 10) 
								+ (Math.abs(Math.sin(index/4)) * 20) 
								+ integerInRange(0, 5)
								* (index / 10.0)
							) 
						);
			}
			
			// default to random
			return integerInRange(0, 100);
		}
		

	}
	
	
	/******************************************************************************
	 * Creates a random json array of people with various properties.
	 * 
	 * @param count
	 ******************************************************************************/
	public static JsonObject jsonObjectMightyPerson() { 
		
		return jsonObjectMightyPerson(0);
		
	}
	
	/******************************************************************************
	 * Creates a random json array of people with various properties.
	 * 
	 * @param count
	 ******************************************************************************/
	public static JsonObject jsonObjectMightyPerson(int nullRatioPercent) { 
		long currentTime = new Date().getTime();
		
		JsonObject object = new JsonObject();
		
		String id = UUID.randomUUID().toString().substring(0, 22);
		object.addProperty("ID",  id);
		object.addProperty("FIRSTNAME", CFW.Random.firstnameOfGod());
		object.addProperty("LASTNAME", CFW.Random.lastnameSweden(nullRatioPercent));
		object.addProperty("LOCATION", CFW.Random.mythicalLocation(nullRatioPercent));

		object.addProperty("LIKES_TIRAMISU", CFW.Random.bool(nullRatioPercent));
		object.addProperty("LAST_LOGIN", currentTime-(CFW.Random.longInRange(100, 10000)*1000000) );
		object.addProperty("URL", "http://www.example.url/mightyperson?id="+id);
		object.addProperty("VALUE", CFW.Random.integerInRange(1, 100));

		
		return object;
	}
	
	/******************************************************************************
	 * Creates a random json array of people with various properties.
	 * 
	 * @param count
	 ******************************************************************************/
	private static final String[] ticketStatus = new String[] { "New", "New", "Open", "Open", "Open", "Open", "Blocked", "In Progress", "Rejected", "Closed"};
	public static JsonObject jsonObjectSupportTickets() { 
		long currentTime = new Date().getTime();
		
		JsonObject object = new JsonObject();
		
		//--------------------------------------
		// Base Values
		String id = UUID.randomUUID().toString().substring(0, 22);
		object.addProperty("LINK", "http://serviceportal.example.url/ticket?id="+id);
		object.addProperty("TICKET_ID",  "TKT-00"+integerInRange(10000, 99999));
		String status = fromArray(ticketStatus);
		object.addProperty("STATUS",  status);
		object.addProperty("PRIORITY",  integerInRange(1, 9));
		object.addProperty("TITLE",  fromArray(firstWorldProblemTitles));
		object.addProperty("SERVICE",  ultimateServiceName());
		object.addProperty("USER_ID", "u"+integerInRange(10000, 99999) );
		object.addProperty("USERNAME", CFW.Random.lastnameSweden().toUpperCase()+" "+CFW.Random.firstnameOfGod());
		
		//--------------------------------------
		// Assignee: 50% Unassigned when Status == New
		if(status.equals("New") && integerInRange(0, 100) > 50) { 
			object.add("ASSIGNEE_ID", JsonNull.INSTANCE );
			object.add("ASSIGNEE_NAME", JsonNull.INSTANCE); 
		}else {
			object.addProperty("ASSIGNEE_ID", "u"+integerInRange(10000, 99999) );
			object.addProperty("ASSIGNEE_NAME", CFW.Random.lastnameSweden().toUpperCase()+" "+CFW.Random.firstnameOfGod());
		}
		
		//--------------------------------------
		// Times
		int createdOffsetMinutes = CFW.Random.integerInRange(200, 10000);
		long createdMillis = CFWTimeUnit.m.offset(currentTime, createdOffsetMinutes);
		int updatedOffsetMinutes = CFW.Random.integerInRange(10, createdOffsetMinutes-(createdOffsetMinutes/6));
		long updatedMillis = CFWTimeUnit.m.offset(currentTime, updatedOffsetMinutes);
		
		object.addProperty("TIME_CREATED", createdMillis );
		object.addProperty("LAST_UPDATED", updatedMillis );
		
		//--------------------------------------
		// Health
		object.addProperty("HEALTH", CFW.Random.integerInRange(1, 100));

		
		return object;
	}
	/******************************************************************************
	 * Creates a random json array of various random number data.
	 * 
	 * @param count
	 ******************************************************************************/
	public static JsonObject jsonObjectNumberData(int nullRatioPercent) { 

		//-------------------------------------------
		// Prepare values in thousand steps
		double multiplier = Math.pow(1000, integerInRange(0, 4));
		double thousands = integerInRange(0, 1000) * multiplier;
		BigDecimal tiny = bigDecimal(0, 1000).setScale(12).divide( new BigDecimal(multiplier+1000), CFW.Math.ROUND_UP );
		
		JsonObject object = new JsonObject();
		
		object.addProperty("UUID", UUID.randomUUID().toString());
		object.addProperty("THOUSANDS",   thousands);
		object.addProperty("FLOAT",   CFW.Random.floatInRange(1, 10000000));
		object.addProperty("TINY_DECIMAL",   tiny);
		object.addProperty("BIG_DECIMAL",   CFW.Random.bigDecimal(1, 1000000, 2));
		

		return object;
	}
	
	/******************************************************************************
	 * Creates a random json array of various random array data.
	 * 
	 * @param count
	 ******************************************************************************/
	public static JsonObject jsonObjectArrayData(int nullRatioPercent) { 


		JsonObject object = new JsonObject();
		
		object.add("ARRAY_NUMBERS", jsonArrayOfIntegers(12,0,100));
		object.add("ARRAY_CHARACTERS", jsonArrayOfCharacters(10));
		object.add("ARRAY_STRINGS", jsonArrayOfRandomStrings(6, 6));
		object.add("ARRAY_MIXED", jsonArrayOfMixedTypes(5, 8));
		return object;
	}
	
	/******************************************************************************
	 * Creates a random json array of various data.
	 * 
	 * @param count
	 ******************************************************************************/
	public static JsonObject jsonObjectVariousData(int nullRatioPercent) { 

		JsonObject object = new JsonObject();
		
		object.addProperty("UUID", UUID.randomUUID().toString());
		object.addProperty("BOOLEAN_STRING", ""+CFW.Random.bool(nullRatioPercent));
		object.add("ALWAYS_NULL", JsonNull.INSTANCE);
		object.addProperty("COLOR", colorName(nullRatioPercent));
		object.addProperty("FRUIT", fruitName(nullRatioPercent));
		object.addProperty("STATUS", fromArray(new String[] {"Excellent", "Good", "Warning", "Emergency", "Danger"}));

		return object;
	}

	
}
