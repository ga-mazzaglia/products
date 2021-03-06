var movementListController = {

    userInfo: {
        id: undefined
    },

    init: function () {
        console.log("movementListController.init()");
        jQuery(".row-mov").click(function () {
            var id = jQuery(this).attr("id");
            jQuery("#btns_" + id).toggle();
        });
        movementListController.initSearchBox();
    },

    edit: function (id) {
        location = "/movement/edit/" + id;
    },

    delete: function (id) {
        Utils.showConfirm("Eliminar", "Está seguro de continuar?", "success", function () {
            Rest.doPost("/movement/delete", {id: id}, function (data) {
                if (data.status == 200) {
                    location = "/movement/list"
                } else {
                    jQuery("#errorMessage").html(data.responseJSON.response.message);
                }
                jQuery("#buttons").show();
            });
        });
    },

    showModalAddTags: function (movId) {
        var tags = jQuery("#mov_tag_list_" + movId).find("span")
        jQuery(".btn-tag").removeClass("btn-success").addClass("btn-primary");
        jQuery("#movAddTagId").val(movId)
        jQuery(tags).each(function (index, value) {
            var tagId = value.id.split("_")[2]
            jQuery("#modal_tag_" + tagId).removeClass("btn-primary").addClass("btn-success");
        })

        jQuery("#tagsModal").click()
    },

    clickAddTag: function (item, tagId) {
        jQuery("#btnSaveAddTag").hide()
        var add = false;
        if (jQuery(item).hasClass("btn-primary")) {
            jQuery(item).removeClass("btn-primary").addClass("btn-success");
            jQuery("#tags_selected").append("<input name='tags' value='" + tagId + "' id='" + tagId + "'/>");
            add = 1;
        } else {
            jQuery(item).removeClass("btn-success").addClass("btn-primary");
            jQuery("#tags_selected #" + tagId).remove();
            add = 0;
        }
        var movId = jQuery("#movAddTagId").val()

        Rest.doPost("/movement/tag", {mov_id: movId, tag_id: tagId, added: add}, function (data) {
            console.log(data, data.response, data.response.added == true);
            jQuery("#btnSaveAddTag").show()
            if (data.status == 200) {
                if (data.response.added) {
                    jQuery("#mov_tag_list_" + movId).append('<span id="tag_' + movId + '_' + tagId + '" style="padding-right: 10px;color: grey;"><i style="color: grey" class="fa fa-tag"></i> ' + data.response.tag.detail + '</span>')
                } else {
                    jQuery("#mov_tag_list_" + movId).find("#tag_" + movId + "_" + tagId).remove()
                }
            }
        })
    },

    showDetails: function (id) {
        Rest.doGet("/movement/" + id, function (data) {
            console.log(data.response);
            var userEquals = "";
            if (data.status == 200) {
                var html = "<div>";
                var quantity = data.response.users.length + 1;
                var amount = parseFloat(data.response.amount / quantity).toFixed(2);

                if (quantity != 0) {
                    userEquals = movementListController.userInfo.id == data.response.user.id ? "my_part" : "";
                    html += "   <div class='row_mov_part " + userEquals + "'>";
                    html += "       <div style='float: left;'>" + data.response.user.name + "</div>";
                    html += "       <div style='float: right;'>$ " + amount + "</div>";
                    html += "   </div>";
                    html += "   <div style='clear: both'></div>";
                }
                jQuery(data.response.users).each(function (index, item) {
                    userEquals = movementListController.userInfo.id == item.id ? "my_part" : "";
                    html += "   <div class='row_mov_part " + userEquals + "'>";
                    html += "       <div style='float: left;'>" + item.name + "</div>";
                    html += "       <div style='float: right;'>$ " + amount + "</div>";
                    html += "   </div>";
                    html += "   <div style='clear: both'></div>";
                });
                if (quantity != 0) {
                    html += "   <div class='row_mov_part row_mov_part_total'>";
                    html += "       <div style='float: left;'>TOTAL</div>";
                    html += "       <div style='float: right;'>$ " + parseFloat(data.response.amount).toFixed(2) + "</div>";
                    html += "   </div>";
                    html += "   <div style='clear: both'></div>";
                    html += "</div>";
                    Utils.showAlert("Detalle", html)
                }
            }
        })
    },

    showSearchBox: function () {
        jQuery("#search_content").toggle();
        jQuery("#search").focus();
    },

    initSearchBox: function () {
        var showSearchBox = false;

        var searchValue = jQuery("#search").val();
        var dateIni = jQuery("#dateIni").val();
        var dateEnd = jQuery("#dateEnd").val();

        if (searchValue != "") {
            showSearchBox = true;
        }
        jQuery(".btn-filter-period").removeClass("btn-success");
        jQuery(".btn-filter-period").addClass("btn-primary");
        jQuery(".btn-filter-period").click(function () {
            jQuery("#dateIni").val("");
            jQuery("#dateEnd").val("");
            jQuery("#filter_perdiod").val(jQuery(this).attr("data-filter"));
            jQuery("#btn-search").click();
        });
        jQuery("#btn-clear").click(function () {
            location = "/movement/list"
        });
        var filterPerdiod = Utils.getUrlParam("filter_perdiod");
        if (filterPerdiod != null && filterPerdiod && dateIni == "" && dateEnd == "") {
            jQuery(".btn-filter-period").removeClass("btn-success");
            jQuery(".btn-filter-period").addClass("btn-primary");
            jQuery(".btn-filter-period[data-filter=" + filterPerdiod + "]").addClass("btn-success");
            showSearchBox = true;
        }
        if (jQuery("#dateIni").val() || jQuery("#dateEnd").val()) {
            showSearchBox = true;
        }
        if (!showSearchBox) {
            jQuery(".btn-filter-period[data-filter=thismonth]").addClass("btn-success");
        }
    },

    clickTag: function (id) {
        var tag = "#tag_" + id;
        if (jQuery(tag).hasClass("btn-primary")) {
            jQuery(tag).removeClass("btn-primary").addClass("btn-success");
        } else {
            jQuery(tag).removeClass("btn-success").addClass("btn-primary");
        }

        if (jQuery(".btn-tag.btn-success").length != 0) {
            jQuery("#tags_filters").html("");
            var tags = [];
            jQuery(".btn-tag.btn-success").each(function (index, item) {
                tags.push(jQuery(item).attr("tag-id"))
                jQuery("#tags_filters").append("<input type='hidden' name='tags' value='" + jQuery(item).attr("tag-id") + "'>");
            });
            jQuery("#btn-search").click();
        } else {
            jQuery("#tags_filters").html("");
            jQuery("#btn-search").click();
        }
    },

    refresh: function () {
        location.reload(true)
    },

}

jQuery(document).ready(movementListController.init);
