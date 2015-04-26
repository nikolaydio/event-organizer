$(document).ready(function(){
  "use strict"

  //General access data
  var host = "";//"http://private-5ee78-eventorg.apiary-mock.com";
  var user = "dfb9153d-2172-41ba-900b-8f56106f6dc7";
  var read_stream = "dfb9153d-2172-41ba-900b-8f56106f6dc7";
  var write_stream = "f3be18bf-408f-451f-8d10-e2fee82bb1a9";
  var loaded_msgs = [];

  //Updating data in data explorer
  function update_content_data() {
    $.get(host + "/api/user/feed").then(function(data) {
      loaded_msgs = data;
      render_data();
    })

  }


  update_content_data();
  setInterval(update_content_data,5000);

  //adding new messages
  $("#send-msg").click(function(){
    $.post(host + "/api/user/feed",
           {
             "tags": get_tags(),
             "value": { "msg": $("#content-msg").val() }
           }
          ).done(function(data) {
      $("#content-msg").val("");
      console.log(data);
    });
  });

  function render_msg(elem) {
    var jq = $("<div>").attr("class", "alert alert-warning");
    var add = "";
    if(elem.value.msg != null) {
      add = elem.value.msg;
    }else{
      add = elem.value;
    }
    jq.text(add);
    $.each(elem.tags, function(index, v) {
      jq.append($("<span>").attr("class", "label label-primary").attr("style", "margin-left:5px").text(v));
    });
    return jq;
  }

  function filter_msg(msg, tags) {
    var matches = 0;
    if(tags.length == 0) {
      return true;
    }
    for(var i = 0; i < tags.length; ++i) {
      for(var u = 0; u < msg.tags.length; ++u) {
        if(tags[i] == msg.tags[u]) {
          matches++;
        }
      }
    }
    if(matches == tags.length) {
      return true;
    }
    return false;
  }

  function get_tags() {
    var stags = [];
    $("#selected-tags > span").each(function() {
      stags.push($(this).text());
    });
    return stags;
  }

  function render_data() {
    var cont = $("#content-data");
    cont.empty();
    $.each(loaded_msgs, function(index, value) {
      if (filter_msg(value, get_tags())) {
        cont.append(render_msg(value));
      }
    });
  }

    //Updating tags
  function update_tags() {
    $.get(host + "/api/user/tags").then(function(data) {
      console.log(data);
      var tags = $("#tags");
      tags.empty();
      $.each(data, function(index, value) {
        var tag = $("<span class=\"label label-primary\" style=margin-left:5px>").text(value);
        var move_to_selected = function() {
          tag.click(move_to_other);
          $("#selected-tags").append(tag);
          render_data();
        };
        var move_to_other = function() {
          tag.click(move_to_selected);
          tags.append(tag);
          render_data();
        };
        tag.click(move_to_selected);
        tags.append(tag);
      });
    });
  }
  update_tags();

S

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
