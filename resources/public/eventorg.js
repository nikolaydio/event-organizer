
$(document).ready(function(){
    "use strict"
    $("#send-event").click(function(){
      $.post("/api/stream/" + $("#stream-id").val(),
        {
          msg: $("#event-text").val()
        }
      ).done(function(data) {
        console.log(data);
      });
    });

  $("#refresh-events").click(function() {
    $.get("/api/stream/" + $("#stream-id").val(),
          {}
    ).done(function(data){
      $("#events").empty();
      console.log(data);
      $.each(data, function(key, entry) {
        $("#events").append($("<p>").text(entry));
      });
    });
  })
});
