$(document).ready(function(){
  "use strict"

  //General access data
  var host = "http://private-5ee78-eventorg.apiary-mock.com";
  var user = "dfb9153d-2172-41ba-900b-8f56106f6dc7";
  var read_stream = "dfb9153d-2172-41ba-900b-8f56106f6dc7";
  var write_stream = "f3be18bf-408f-451f-8d10-e2fee82bb1a9";

  //Updating data in data explorer
  function update_content_data() {
    $.get(host + "/api/streams/" + read_stream).then(function(data) {
      var element = $("#content-data");
      element.empty();
      $.each( data, function(index, value) {
        var add = "";
        if(value.value.msg) {
          add = value.value.msg;
        }else{
          add = value.value;
        }
        element.append($("<div class=\"alert alert-warning\">").text(add));
      })
    })

  }


  update_content_data();
  setInterval(update_content_data,5000);

  //adding new messages
  $("#send-msg").click(function(){
    $.post(host + "/api/streams/" + write_stream,
           {
             msg: $("#content-msg").val()
           }
          ).done(function(data) {
      $("#content-msg").val("");
      console.log(data);
    });
  });


    //Updating tags
  function update_tags() {
    $.get(host + "/api/users/" + user + "/tags").then(function(data) {
      var tags = $("#tags");
      tags.empty();
      $.each(data, function(index, value) {
        tags.append($("<span class=\"label label-primary\" style=margin-left:5px> ").text(value));
      });
    });
  }
  update_tags();



    $("#send-event").click(function(){
      $.post("/api/streams/" + $("#stream-id").val(),
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
