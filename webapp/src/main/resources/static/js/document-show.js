
$( document ).ready( function() {

	// File upload button: Clicking on the button simulates the click on the native file button
	document.getElementById('fileUploadButton').addEventListener('click', function() {
		document.getElementById('files').click();
	});

	// File Upload: Selection of files in the file selection dialog sends the form immediately
	document.getElementById('files').addEventListener('change', function() {
		document.getElementById('fileUploadForm').submit();
	});

	// Comments: Loading the page with anchor marks the comment with background color
	// Example: http://localhost:8080/document/show/[ID]#comment-[COMMENT-ID]
	if(window.location.hash) {
		var hash = window.location.hash.substring(1);
		$('#' + hash).addClass('bg-light');
	}

	// Comments: Clicking on a permlink with an anchor does not reload the page.
	// Here the associated comment is marked with a background color.
	// Previously set markings are also removed here.
	$('.permlink-comment-btn').click(function() {
		// remove previously set markings
		$('[id^=comment-]').removeClass('bg-light');

		// mark comment with background color
		var commentId = $(this).data('id');
		$('#comment-' + commentId).addClass('bg-light');
	});

	// Comments: Show the form for adding a new comment.
	$('.add-comment-btn').click(function() {
		// show the form
		$('#newCommentForm').show('slow');

		// set the focus on the input field
		$('#newCommentInput').focus();

		// deactivate the buttons that led to this action
		$('.add-comment-btn').attr('disabled', 'disabled');
	});

	// Comments: Hide the form for adding a new comment
	$('#newCommentCancelButton').click(function() {
		// hide the form
		$('#newCommentForm').hide('slow');

		// empty the input filed
		$('#newCommentInput').val('');

		// activate the buttons to add a new comment
		$('.add-comment-btn').removeAttr('disabled');
	});

	// Comments: Show the form for editing a comment
	$('.edit-comment-btn').click(function() {
		var commentId = $(this).data('id');

		// Check if the form already exists
		if ($('#editCommentForm-' + commentId).length > 0)
		{
			// Only set the focus if the form already exists
			$('#editCommentInput-' + commentId).focus();
		}
		else
		{
			var commentText = $('#commentText-' + commentId).html();

			var html = '';
			html += '<form action="/comment/update" method="post" id="editCommentForm-' + commentId + '">';
			html += '	<input type="hidden" id="id" name="id" value="' + commentId + '">';
			html += '	<input type="hidden" id="document" name="document" value="' + documentId + '">';
			html += '	<div class="form-group row">';
			html += '		<div class="col-sm-12">';
			html += '			<textarea class="form-control" id="editCommentInput-' + commentId + '" rows="5" name="text">' + commentText + '</textarea>';
			html += '		</div>';
			html += '	</div>';
			html += '	<div class="form-group row">';
			html += '		<div class="col-sm-12">';
			html += '			<input type="submit" class="btn btn-sm btn-primary" value="' + messages_defaultButtonSaveLabel + '">';
			html += '			<button type="button" id="editCommentCancelButton-' + commentId + '" class="btn btn-sm btn-outline-secondary">' + messages_defaultButtonCancelLabel + '</button>';
			html += '		</div>';
			html += '	</div>';
			html += '</form>';

			// Add the newly created form to the comment area
			$('#comment-' + commentId).append(html);

			// set the focus on the input field
			$('#editCommentInput-' + commentId).focus();

			// register an action to delete the form by clicking on the cancel button
			$('#editCommentCancelButton-' + commentId).click(function() {
				$('#editCommentForm-' + commentId).remove();
			});
		}
	});

});