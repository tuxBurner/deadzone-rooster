var roosterGuiHandler = {

  /**
   * Gets the avaible factions from the backend and fills them into the select
   */
  getAndFillFactions: function () {
    jsRoutes.controllers.RoosterController.getFactions().ajax({
      success: function (data) {
        $('#rooster_faction_select').html('');

        $.each(data, function (idx, faction) {
          $('#rooster_faction_select').append('<option value="' + faction.name + '">' + faction.name + '</option>');
        });

        roosterGuiHandler.getAndFillFactionTroopSelect();
      }
    });
  },

  /**
   * Gets the avaible troops for the selected faction from the backend and displays them in the add troop drop down
   */
  getAndFillFactionTroopSelect: function () {
    var selectedFaction = $('#rooster_faction_select').val();
    jsRoutes.controllers.RoosterController.getSelectTroopsForFaction(selectedFaction).ajax({
      success: function (data) {
        $('#rooster_addTroop_select').html('');

        $.each(data, function (idx, troop) {
          $('#rooster_addTroop_select').append('<option value="' + troop.name + '">' + troop.name + ' (' + troop.modelType + ')' + ' Points: ' + troop.points + '</option>');
        });
      }
    });
  },

  /**
   * Adds the selected troop to the army list
   */
  addSelectedTroopToArmy: function() {
    var troopToAdd = {
      faction: $('#rooster_faction_select').val(),
      troop: $('#rooster_addTroop_select').val()
    };
    jsRoutes.controllers.RoosterController.addTroopToSelection().ajax({
      data: JSON.stringify(troopToAdd),
      contentType: "application/json; charset=utf-8",
      success: function(data) {
       console.error(data);
      }
    });
  }
};


$(function () {

  roosterGuiHandler.getAndFillFactions();

  $('#rooster_faction_select').on('change', function() {
    roosterGuiHandler.getAndFillFactionTroopSelect();
  });

  $('#rooster_addTroop_btn').on('click', function() {
    roosterGuiHandler.addSelectedTroopToArmy();
  });

});                                                              