$(document).ready(function() {
    //function upd() {
    //}
    function  callback2(bugname, element) {
        var frm = new Array();
        var to = new Array();

        element.forEach(function (e) {
            $.get("derby/" + bugname + "/from/" + e, function (response) {
                frm[e] = response;
            });
            $.get("derby/" + bugname + "/to/" + e, function (response2) {
                to[e] = response2;
            });
            setTimeout(function(){
                while (frm[e] == undefined && to[e] == undefined);
                $(".loader").css('visibility', 'hidden');
                callback(e, to[e],frm[e]); return; }, 3000);
        });
    }

    function  callback(filename, to, frm) {
        //alert(to);

        var base = difflib.stringAsLines(frm);
        var newtxt = difflib.stringAsLines(to);

        // create a SequenceMatcher instance that diffs the two sets of lines
        var sm = new difflib.SequenceMatcher(base, newtxt);

        // get the opcodes from the SequenceMatcher instance
        // opcodes is a list of 3-tuples describing what changes should be made to the base text
        // in order to yield the new text
        var opcodes = sm.get_opcodes(refsToJS);
        var diffoutputdiv = document.getElementById("diffoutput");
//            while (diffoutputdiv.firstChild) diffoutputdiv.removeChild(diffoutputdiv.firstChild);
        var contextSize = $("contextSize").value;
        contextSize = contextSize ? contextSize : null;
        // build the diff view and add it to the current DOM
        diffoutputdiv.appendChild(diffview.buildView({
            baseTextLines: base,
            newTextLines: newtxt,
            opcodes: opcodes,
            // set the display titles for each resource
            baseTextName: "Base Text" + filename,
            newTextName: "New Text" + filename,
            contextSize: contextSize,
            viewType: $("inline").checked ? 1 : 0
        }));
        diffoutputdiv.lastChild.id = filename;
    }
    // scroll down to the diff view window.
    //location = url + "#diff";

    var diffoutputdiv = document.getElementById("diffoutput");
    diffoutputdiv.lastChild;
    while (diffoutputdiv.firstChild) diffoutputdiv.removeChild(diffoutputdiv.firstChild);


    $(".loader").css('visibility', 'visible');
    frm = undefined;
    to = undefined;
    var files = [];
    var changebug = document.getElementById("changebug").value;
    $.ajax({
        url: "derby/" + changebug + "/from",
        //async: false,
        success: function (data) {
            $(data).find("a:contains(.java)").each(function () {
                // var cn = $(this).attr("href");
                // alert(cn);
                // alert(refs[cn]);
                files.push($(this).attr("href"));
            });
            callback2(changebug, files);
        }

    });

});