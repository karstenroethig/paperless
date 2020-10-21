
$( document ).ready( function() {
	$('#search-card').hide();
});

function postShowSearchParams()
{
//	$('.select2-multiple').select2();
}

function postDeleteSearchParams()
{
	$('.select2-multiple').val(null).trigger('change');
}