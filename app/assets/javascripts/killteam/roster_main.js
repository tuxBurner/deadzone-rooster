let ktRosterGuiHandler = {

  /**
   * Gets the available factions from the backend and fills them into the select
   */
  getAndFillFactions: function () {
    jsRoutes.controllers.KTRosterController.getFactions().ajax({
      success: function (data) {
        $('#roster_faction_select').html('');

        $.each(data, function (idx, faction) {
          $('#roster_faction_select').append(`<option value="${faction.name}">${faction.name}</option>`);
        });

        ktRosterGuiHandler.getAndFillFactionTroopSelect();
      }
    });
  },

  /**
   * Gets the available troops for the selected faction from the backend and displays them in the add troop drop down
   */
  getAndFillFactionTroopSelect: function () {
    let selectedFaction = $('#roster_faction_select').val();
    jsRoutes.controllers.KTRosterController.getSelectTroopsForFaction(selectedFaction).ajax({
      success: function (data) {
        $('#roster_addTroop_select').html('');
        $.each(data , function (idx, troop) {
          $('#roster_addTroop_select').append(`<option value="${troop.name}">${troop.name} (${troop.points})</option>`);
        });

        ktRosterGuiHandler.getAndFillSpecialistsSelect();
      }
    });
  },


  /**
   * Gets all specialists the troop can have
   */
  getAndFillSpecialistsSelect :  function() {
    let selectedFaction = $('#roster_faction_select').val();
    let selectedTroop = $('#roster_addTroop_select').val();
    jsRoutes.controllers.KTRosterController.getSelectSpecialistsForTroop(selectedTroop,selectedFaction).ajax({
      success: function (data) {
        if(data.length === 1) {
          $('#roster_specialists_select_wrapper').hide();
        } else {
          $('#roster_specialists_select_wrapper').show();
        }

        $('#roster_specialists_select').html('');
        $.each(data , function (idx, specialist) {
          let selected = (idx === 1) ? 'selected' : '';
          $('#roster_specialists_select').append(`<option value="${specialist}" ${selected}>${specialist}</option>`);
        });
      }
    });

  },

  /**
   * Adds the selected troop to the army list
   */
  addSelectedTroopToArmy: function () {
    let troopToAdd = {
      faction: $('#roster_faction_select').val(),
      troop: $('#roster_addTroop_select').val(),
      specialist: $('#roster_specialists_select').val()
    };
    jsRoutes.controllers.KTRosterController.addTroopToArmy().ajax({
      data: JSON.stringify(troopToAdd),
      contentType: "application/json; charset=utf-8",
      success: function (data) {
        window.location.reload();
      }
    });
  }

};


$(function () {

  ktRosterGuiHandler.getAndFillFactions();

  $('#roster_faction_select').on('change', () => ktRosterGuiHandler.getAndFillFactionTroopSelect());

  $('#roster_troop_type_select').on('change', () => ktRosterGuiHandler.fillTroopSelectForType());

  $('#roster_addTroop_select').on('change', () => ktRosterGuiHandler.getAndFillSpecialistsSelect());

  $('#roster_addTroop_btn').on('click', () => ktRosterGuiHandler.addSelectedTroopToArmy());
});                                                              