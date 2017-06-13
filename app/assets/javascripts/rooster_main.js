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
        roosterGuiHandler.getCurrentArmy();
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
          $('#rooster_addTroop_select').append('<option value="' + troop.name + '">' + troop.modelType + ' | Name: ' + troop.name + ' | Points: ' + troop.points + '</option>');
        });
      }
    });
  },

  /**
   * Adds the selected troop to the army list
   */
  addSelectedTroopToArmy: function () {
    var troopToAdd = {
      faction: $('#rooster_faction_select').val(),
      troop: $('#rooster_addTroop_select').val()
    };
    jsRoutes.controllers.RoosterController.addTroopToSelection().ajax({
      data: JSON.stringify(troopToAdd),
      contentType: "application/json; charset=utf-8",
      success: function (data) {
        roosterGuiHandler.displayCurrentArmyData(data);
      }
    });
  },

  /**
   * Gets the current army as json
   */
  getCurrentArmy: function () {
    jsRoutes.controllers.RoosterController.getArmy().ajax({
      success: function (data) {
        roosterGuiHandler.displayCurrentArmyData(data);
      }
    });
  },

  /**
   * Removes the selected troop from the army
   * @param uuid
   */
  removeTroopFromArmy: function (uuid) {
    jsRoutes.controllers.RoosterController.removeTroopFromArmy(uuid).ajax({
      success: function (data) {
        roosterGuiHandler.displayCurrentArmyData(data);
      }
    });
  },


  /**
   * Gets needed weapon/item informations from the backend and displays the edit popup
   * @param uuid
   */
  displayEditPopup: function (uuid) {
    jsRoutes.controllers.RoosterController.getWeaponsForTroop(uuid).ajax({
      success: function (data) {
        console.error(data);

        var modalBodyContent = '';
        if(data.free.length !== 0) {
          $.each(data.free,function(idx,weapon) {
            modalBodyContent+=weapon.name;
          });
        }

        $('#rooster_troop_edit_body').html(modalBodyContent);
        $('#rooster_troop_edit_modal').modal('show');
      }
    });
  },

  /**
   * Populates the army table with the current data
   * @param armyData
   */
  displayCurrentArmyData: function (armyData) {

    $('#rooster_army_points').text(armyData.points);
    $('#rooster_army_faction').text(armyData.faction);

    $('#rooster_faction_select').attr('disabled', armyData.faction !== '');

    if (armyData.faction !== '') {

    } else {
    }

    $('#rooster_troop_tbody').html('');
    var tableContent = '';
    $.each(armyData.troops, function (idx, troop) {

      var abilitiesContent = '';
      $.each(troop.abilities, function (idx, ability) {
        abilitiesContent += ability.name;
        if (ability.defaultVal !== 0) {
          abilitiesContent += ' (' + ability.defaultVal + ')';
        }
        abilitiesContent += '<br />';
      });

      var weaponsContent = '';
      $.each(troop.weapons, function (idx, weapon) {
        weaponsContent += weapon.name + ' <span class="badge">R';
        if (weapon.shootRange === 0) {
          weaponsContent += 'F';
        } else {
          weaponsContent += weapon.shootRange;
        }
        weaponsContent += '</span>';
        if (weapon.armorPircing !== 0) {
          weaponsContent += ',AP' + weapon.armorPircing;
        }
        $.each(weapon.abilities, function (idx, ability) {
          weaponsContent += ',' + ability.name;
          if (ability.defaultVal !== 0) {
            weaponsContent += ' (' + ability.defaultVal + ')';
          }
        });
        weaponsContent += '<br />';
      });

      var reconArmySpecialContent = '';
      if (troop.recon !== 0) {
        reconArmySpecialContent += troop.recon + '+ / ' + troop.armySpecial;
      }

      var itemsContent = '';
      $.each(troop.items, function (idx, item) {
        itemsContent += item.name + '<br />';
      });


      tableContent += '<tr data-uuid="' + troop.uuid + '">';

      tableContent += '<td>' + troop.name + '</td>';
      tableContent += '<td>' + troop.modelType + '</td>';
      tableContent += '<td>' + troop.points + '</td>';
      tableContent += '<td>' + troop.victoryPoints + '</td>';
      tableContent += '<td>' + troop.armour + '</td>';
      tableContent += '<td>' + troop.size + '</td>';
      tableContent += '<td>' + troop.speed + ' - ' + troop.sprint + '</td>';
      tableContent += '<td>' + troop.shoot + '+</td>';
      tableContent += '<td>' + troop.fight + '+</td>';
      tableContent += '<td>' + troop.survive + '+</td>';
      tableContent += '<td>' + abilitiesContent + '</td>';
      tableContent += '<td>' + weaponsContent + '</td>';
      tableContent += '<td>' + itemsContent + '</td>';
      tableContent += '<td>' + reconArmySpecialContent + '</td>';

      tableContent += '<td>';
      tableContent += '<button class="btn btn-info btn-sm rooster_edit_btn"><span class="glyphicon glyphicon-pencil"></span></button> ';
      tableContent += '<button class="btn btn-danger btn-sm rooster_del_btn"><span class="glyphicon glyphicon-trash"></span></button>';
      tableContent += '</td>';

      tableContent += '</tr>';
    });

    $('#rooster_troop_tbody').html(tableContent);
  }
};


$(function () {

  roosterGuiHandler.getAndFillFactions();

  $('#rooster_faction_select').on('change', function () {
    roosterGuiHandler.getAndFillFactionTroopSelect();
  });

  $('#rooster_addTroop_btn').on('click', function () {
    roosterGuiHandler.addSelectedTroopToArmy();
  });

  $(document).on('click', '.rooster_del_btn', function () {
    var uuid = $(this).closest('tr').attr('data-uuid');
    roosterGuiHandler.removeTroopFromArmy(uuid);
  });

  $(document).on('click', '.rooster_edit_btn', function () {
    var uuid = $(this).closest('tr').attr('data-uuid');
    roosterGuiHandler.displayEditPopup(uuid);
  });

});                                                              