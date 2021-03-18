/**
 * Executes some initialization operations when the DOM is fully loaded.
 */
$( document ).ready( function() {

	if (automaticRefresh)
	{
		setTimeout(function() {
			location.reload();
		}, 5000);
	}
});