
$( document ).ready( function() {

	document.getElementById('fileUploadButton').addEventListener('click', function() {
		document.getElementById('files').click();
	});

	document.getElementById('files').addEventListener('change', function() {
		document.getElementById('fileUploadForm').submit();
	});
});