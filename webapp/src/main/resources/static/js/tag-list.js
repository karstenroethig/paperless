
$( document ).ready( function() {

	setTimeout( function() {

		$('.ajaxFetchUsage').each(function() {

			var element = $(this);
			element.find('.fa-question').hide();
			element.find('.fa-spinner').show();

			$.ajax({
				url: '/api/1.0/tag/' + element.data('id') + '/usage',
				dataType: 'json',
				cache: false
			}).done(function(data) {
				element.find('.fa-spinner').hide();
				element.find('a').html(data.usage);
				element.find('a').show();
			}).fail(function(jqXHR, textStatus, errorThrown) {
//				alert(jqXHR.responseText);
				var responseObj = $.parseJSON(jqXHR.responseText);
				element.find('.fa-spinner').hide();
				element.find('.fa-exclamation-circle').show();
				element.find('.fa-exclamation-circle').prop('title', responseObj.message);
			});
		});
	}, 3000);

});