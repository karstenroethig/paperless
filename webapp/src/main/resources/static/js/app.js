
$( document ).ready( function() {

	$('[data-bs-toggle="popover"]').popover();

	// initialize summernote
	$('.summernote').summernote({
		toolbar: [
			['fontStyle', ['color', 'bold', 'italic', 'underline', 'strikethrough', 'superscript', 'subscript', 'clear']],
			['paragraphStyle', ['style', 'ol', 'ul', 'paragraph']],
			['insert', ['hr', 'table', 'link', 'picture']],
			['misc', ['fullscreen', 'codeview', 'undo', 'redo']]
		]
	});

	// delete modals: transfer the id to the modal form
	$( '.deleteModal' ).on( 'show.bs.modal', function( event ) {
		var button = $( event.relatedTarget ); // Button that triggered the modal
		var id = button.data( 'id' ); // Extract info from data-* attributes

		// Update the modal's content.
		var modal = $( this );
		var link = modal.find( '.btn-danger' );
		var template = link.data( 'href-template' );
		link.attr( 'href', template.replace( '{id}', id ) );
	});

	initializeHelpIcons();

	initializeSearchSortAndPagination();
});

function initializeSearchSortAndPagination()
{
	// searching: show and hide search parameters
	$('#search-button').click(function() {
		if($('#search-card:visible').length) {
			$('#search-card').hide('slow');
		} else {
			$('#search-card').show('slow');
			postShowSearchParams();
		}
	});

	// searching: delete parameters
	$('#search-button-delete-parameters').click(function() {
		$('#search-card form :input').not(':button, :submit, :reset, :hidden, :checkbox, :radio').val('');
		$('#search-card form :checkbox, :radio').prop('checked', false);
		postDeleteSearchParams();
	});

	// paging: reload on pagesize select
	$('select.pagination-pagesize').change(function() {
		var newPageSize = $('select.pagination-pagesize option:selected').attr('value');
		window.location.href = window.location.pathname + '?page=0&size=' + newPageSize + '&sort=' + sortParam;
	});

	// paging: load page by index
	$('input.pagination-page').keypress(function(event) {
		if (event.which == 13) {
			var page = parseInt($('input.pagination-page').val());
			window.location.href = window.location.pathname + '?page=' + (page-1) + '&size=' + pageSize + '&sort=' + sortParam;
			return false;
		}
	});

	// sorting: display sorting sign in sorted column header
	$('table thead th').each(function() {
		var headerSortPropName = $(this).attr('data-sort-prop');
		if (headerSortPropName !== undefined) {
			if (headerSortPropName == sortProperty) {
				$(this).append(sortDesc?'▾':'▴');
			}
		}
	});

	// sorting: reload on click on column header
	$('table thead th').click(function() {
		var headerSortPropName = $(this).attr('data-sort-prop');
		if (headerSortPropName !== undefined) {
			if (headerSortPropName == sortProperty) {
				window.location.href = window.location.pathname + '?page=' + currentPage + '&size=' + pageSize + '&sort=' + headerSortPropName + ',' + (sortDesc ? 'asc' : 'desc');
			} else {
				window.location.href = window.location.pathname + '?page=' + currentPage + '&size=' + pageSize + '&sort=' + headerSortPropName + ',asc';
			}
		}
	});
}

function postShowSearchParams() {}

function postDeleteSearchParams() {}

function initializeHelpIcons()
{
	$('.help-icon').click(function(event) {
		var sectionElement = $('#' + event.target.id + '_content');
		if (sectionElement.length) {
			if (sectionElement.is(':visible')) {
				sectionElement.hide('slow');
			} else {
				sectionElement.show('slow');
			}
		}
	});
}
