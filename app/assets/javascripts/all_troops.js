$(function() {
  $('#troopsArmySelect').on('change', function() {
     var faction = $(this).val();
    window.location = jsRoutes.controllers.HomeController.displayTroopsOfFaction(faction).absoluteURL();

  });
});