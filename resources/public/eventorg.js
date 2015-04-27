$(document).ready(function(){
  "use strict"

  //General access data
  var access_string = "http://93.155.146.63/";
  var host = "";//"http://private-5ee78-eventorg.apiary-mock.com";
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



  //STREAM MANAGEMENT CODE
  function render_streams_list(data) {
    $("#stream-list-container").empty();
    $.each(data, function(index, value) {
      var tag = $("<p>");
      tag.append($("<span>").text(access_string + "/api/streams/" + value.id));
      tag.append($("<span>").text(value.tags));
      tag.append($("<span style='margin: 5px'>").click(function() {
        tag.remove();
        $.ajax({ url: host + "/api/user/streams/" + value.id,
                method: "DELETE"} ).then(function() {
        });
      }).text("X"));
      $("#stream-list-container").append(tag);
    });
  }
  function update_streams_list() {
    $.get(host + "/api/user/streams").then(function(data) {
        render_streams_list(data);
    });
  }

  //attach to toggling button to refresh list
  $("#toggle-streams").click(function() {
    update_streams_list();
  });

  $("#add-stream").click(function() {
    var tags = $("#new-stream-tags").val();
    $("#new-stream-tags").text("");
    var tag_list = tags.split(",");
    $.post(host + "/api/user/streams", {tags: tag_list}).then(function(data) {
      update_streams_list();
    });
  });

});
