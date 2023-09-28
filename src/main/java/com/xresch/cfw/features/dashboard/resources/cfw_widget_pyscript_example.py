from datetime import datetime as dt

def createHTML(title):
	# Never use endless loops, as they will stack when widgets are refreshed
	#while True:
	
	htmlString = "<span><b>"+title+"</b></span><br>";
	
	timestamp = dt.now().strftime("%m/%d/%Y, %H:%M:%S")	   
	if timestamp[-1] in ["0", "2", "4", "6", "8"]:
		htmlString += '<p class="bg-cfw-green text-white">It\'s time for Tiramisu!</p>';
	else:
		htmlString += timestamp
	
	return htmlString;


widgetContent = createHTML("What time is it?")
widgetContent += createHTML("Any other information?")

# use "widget" with DOM attributes to add content to the widget
widget.innerHTML = widgetContent;