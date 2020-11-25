
$( document ).ready( function() {

	var $select = $('.tags-search').selectize();
	var control = $select[0].selectize;

	$('#search-button-delete-parameters').click(function() {
		control.clear();
	});
});
