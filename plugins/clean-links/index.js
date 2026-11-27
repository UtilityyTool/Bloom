// Clean links — removes common tracking parameters from links before they send.
// Permission: messages.edit-outgoing (to read and change the outgoing message).

var TRACKERS = ["si", "utm_source", "utm_medium", "utm_campaign", "utm_term", "utm_content", "fbclid", "igsh", "gclid"];

function clean(text) {
  TRACKERS.forEach(function (t) {
    text = text.replace(new RegExp("([?&])" + t + "=[^&\\s]*", "g"), "$1");
  });
  // Tidy any separator left dangling by a removed parameter.
  return text
    .replace(/\?(\s|$)/g, "$1")
    .replace(/&&+/g, "&")
    .replace(/\?&/g, "?")
    .replace(/&(\s|$)/g, "$1");
}

bloom.on("beforeSend", function () {
  bloom.setOutgoingText(clean(bloom.getOutgoingText()));
});
