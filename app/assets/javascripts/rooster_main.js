var roosterGuiHandler = {

  /**
   * Gets the avaible factions from the backend and fills them into the select
   */
  getAndFillFactions: function() {
    jsRoutes.controllers.RoosterController.getFactions().ajax({
      success: function(data) {
        $('#rooster_faction_select').html('');

        $.each(data, function(idx, faction) {
          $('#rooster_faction_select').append('<option value="' + faction.name + '">' + faction.name + '</option>');
        });

        roosterGuiHandler.getAndFillFactionTroopSelect();
        roosterGuiHandler.getCurrentArmy();
      }
    });
  },

  /**
   * Gets the avaible troops for the selected faction from the backend and displays them in the add troop drop down
   */
  getAndFillFactionTroopSelect: function() {
    var selectedFaction = $('#rooster_faction_select').val();
    jsRoutes.controllers.RoosterController.getSelectTroopsForFaction(selectedFaction).ajax({
      success: function(data) {
        $('#rooster_addTroop_select').html('');

        $.each(data, function(idx, troop) {
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
        roosterGuiHandler.displayCurrentArmyData(data);
      }
    });
  },

  /**
   * Gets the current army as json
   */
  getCurrentArmy: function() {
    var currentFactionName = {
      faction: $('#rooster_faction_select').val()
    };

    jsRoutes.controllers.RoosterController.getArmy().ajax({
      data: currentFactionName,
      contentType: "application/json; charset=utf-8",
      success: function(data) {
        roosterGuiHandler.displayCurrentArmyData(data);
      }
    });
  },

  /**
   * Populates the army table with the current data
   * @param armyData
   */
  displayCurrentArmyData: function(armyData) {
    $('#rooster_troop_tbody').html('');
    if(armyData.troops.length === 0) {
      $('#rooster_troop_tbody').html('<tr><td>????NOOO</td></tr>');
      return;
    }

    console.error(armyData.troops);

    var tableContent = '';
    $.each(armyData.troops, function(idx, troop) {

      var abilitiesContent = '';
      $.each(troop.abilities, function(idx, ability) {
        abilitiesContent += ability.name;
        if(ability.defaultVal !== 0) {
          abilitiesContent += ' (' + ability.defaultVal + ')';
        }
        abilitiesContent += '<br />';
      });

      var weaponsContent = '';
      $.each(troop.weapons, function(idx, weapon) {
        weaponsContent += weapon.name+' R';
        if(weapon.shootRange === 0) {
          weaponsContent += 'F';
        } else {
          weaponsContent += weapon.shootRange;
        }
        if(weapon.armorPircing !== 0) {
          weaponsContent+=',AP'+weapon.armorPircing;
        }
        $.each(weapon.abilities,function(idx,ability){
          weaponsContent += ','+ability.name;
          if(ability.defaultVal !== 0) {
            weaponsContent += ' (' + ability.defaultVal + ')';
          }
        });
        weaponsContent += '<br />';
      });


      tableContent += '<tr>';

      tableContent += '<td>' + troop.name + '</td>';
      tableContent += '<td>' + troop.modelType + '</td>';
      tableContent += '<td>' + troop.points + '</td>';
      tableContent += '<td>' + troop.armour + '</td>';
      tableContent += '<td>' + troop.size + '</td>';
      tableContent += '<td>' + troop.speed + ' - ' + troop.sprint + '</td>';
      tableContent += '<td>' + troop.shoot + '</td>';
      tableContent += '<td>' + troop.fight + '</td>';
      tableContent += '<td>' + troop.survive + '</td>';
      tableContent += '<td>' + abilitiesContent + '</td>';
      tableContent += '<td>' + weaponsContent + '</td>';

      tableContent += '</tr>';
    });

    $('#rooster_troop_tbody').html(tableContent);
  }
};


$(function() {

  roosterGuiHandler.getAndFillFactions();

  $('#rooster_faction_select').on('change', function() {
    roosterGuiHandler.getAndFillFactionTroopSelect();
  });

  $('#rooster_addTroop_btn').on('click', function() {
    roosterGuiHandler.addSelectedTroopToArmy();
  });

});                                                              