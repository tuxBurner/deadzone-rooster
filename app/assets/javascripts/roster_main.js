var rosterGuiHandler = {

  /**
   * Gets the avaible factions from the backend and fills them into the select
   */
  getAndFillFactions: function () {
    jsRoutes.controllers.RosterController.getFactions().ajax({
      success: function (data) {
        $('#rooster_faction_select').html('');

        $.each(data, function (idx, faction) {
          $('#rooster_faction_select').append('<option value="' + faction.name + '">' + faction.name + '</option>');
        });

        rosterGuiHandler.getAndFillFactionTroopSelect();
        rosterGuiHandler.getCurrentArmy();
      }
    });
  },

  /**
   * Gets the avaible troops for the selected faction from the backend and displays them in the add troop drop down
   */
  getAndFillFactionTroopSelect: function () {
    var selectedFaction = $('#rooster_faction_select').val();
    jsRoutes.controllers.RosterController.getSelectTroopsForFaction(selectedFaction).ajax({
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
    jsRoutes.controllers.RosterController.addTroopToArmy().ajax({
      data: JSON.stringify(troopToAdd),
      contentType: "application/json; charset=utf-8",
      success: function (data) {
        rosterGuiHandler.displayCurrentArmyData(data);
      }
    });
  },

  /**
   * Gets the current army as json
   */
  getCurrentArmy: function () {
    jsRoutes.controllers.RosterController.getArmy().ajax({
      success: function (data) {
        rosterGuiHandler.displayCurrentArmyData(data);
      }
    });
  },

  /**
   * Removes the selected troop from the army
   * @param uuid
   */
  removeTroopFromArmy: function (uuid) {
    jsRoutes.controllers.RosterController.removeTroopFromArmy(uuid).ajax({
      success: function (data) {
        rosterGuiHandler.displayCurrentArmyData(data);
      }
    });
  },


  /**
   * Gets needed weapon/item informations from the backend and displays the edit popup
   * @param uuid
   */
  displayEditPopup: function (uuid, troopName) {
    jsRoutes.controllers.RosterController.getWeaponsAndItemsForTroop(uuid).ajax({
      success: function (data) {


        $('#rooster_troop_edit_name').text(troopName);
        $('#rooster_troop_edit_modal').data('troopuuid', uuid);

        var weaponsContent = '';
        weaponsContent += rosterGuiHandler.displayEditWeaponType('Free', 'free', data);
        weaponsContent += rosterGuiHandler.displayEditWeaponType('Fight', 'fight', data);
        weaponsContent += rosterGuiHandler.displayEditWeaponType('Ranged', 'ranged', data);
        $('#rooster_troop_edit_weapons').html(weaponsContent);

        var itemsContent = '';
        $.each(data.items, function (idx, item) {

          var checked = ($.inArray(item.name, data.currentItems) !== -1) ? 'checked' : '';

          itemsContent += '<tr>';
          itemsContent += '<td><input class="edit_troop_slected_item" value="' + item.name + '" type="checkbox" ' + checked + ' /></td>';
          itemsContent += '<td>' + item.name + '</td>';
          itemsContent += '<td>' + item.points + '</td>';
          itemsContent += '<td>' + item.rarity + '</td>';
          itemsContent += '</tr>';
        });
        $('#rooster_troop_edit_items').html(itemsContent);

        $('#rooster_troop_edit_modal').modal('show');
      }
    });
  },

  /**
   * Collects the data for the troop and saves the changes in the backend
   */
  saveChangesToTroop: function () {
    var uuid = $('#rooster_troop_edit_modal').data('troopuuid');
    var dataToSave = {
      selectedWeapons: [],
      selectedItems: []
    };

    $.each($('.edit_troop_slected_weapon:checked'), function (idx, obj) {
      dataToSave.selectedWeapons.push($(obj).val());
    });

    $.each($('.edit_troop_slected_item:checked'), function (idx, obj) {
      dataToSave.selectedItems.push($(obj).val());
    });

    jsRoutes.controllers.RosterController.updateTroopWeaponsAndItems(uuid).ajax({
      data: JSON.stringify(dataToSave),
      contentType: "application/json; charset=utf-8",
      success: function (data) {
        rosterGuiHandler.displayCurrentArmyData(data);
        $('#rooster_troop_edit_modal').modal('hide');
      }
    });
  },

  /**
   * Display a given type of weapons in the edit popup
   * @param headline
   * @param type
   * @param data
   * @returns {string}
   */
  displayEditWeaponType: function (headline, type, data) {
    var content = '';

    if (data.weapons[type].length !== 0) {
      content = '<tr class="info"><th colspan="7">' + headline + '</th></tr>';
      $.each(data.weapons[type], function (idx, weapon) {

        var checked = ($.inArray(weapon.name, data.currentWeapons) !== -1) ? 'checked' : '';

        content += '<tr>';
        content += '<td><input value="' + weapon.name + '" class="edit_troop_slected_weapon" type="checkbox" ' + checked + '/></td>';
        content += '<td>' + weapon.name + '</td>';
        content += '<td>' + weapon.points + '</td>';
        content += '<td>' + weapon.victoryPoints + '</td>';
        content += '<td>' + rosterGuiHandler.weaponRangeForDisplay(weapon) + '</td>';
        content += '<td>' + weapon.armorPircing + '</td>';
        content += '<td>' + rosterGuiHandler.abilitiesForDisplay(weapon.abilities, ',') + '</td>';
        content += '</tr>';
      });
    }
    return content;
  },

  /**
   * Populates the army table with the current data
   * @param armyData
   */
  displayCurrentArmyData: function (armyData) {

    $('#rooster_army_points').text(armyData.points);
    $('#rooster_army_faction').text(armyData.faction);

    //$('#rooster_faction_select').attr('disabled', armyData.faction !== '');

    $('#rooster_troop_tbody').html('');
    var tableContent = '';
    $.each(armyData.troops, function (idx, troop) {

      var weaponsContent = '';
      $.each(troop.weapons, function (idx, weapon) {
        weaponsContent += weapon.name;
        weaponsContent += '<span class="badge">' + rosterGuiHandler.weaponRangeForDisplay(weapon) + '</span>';

        if (weapon.armorPircing !== 0) {
          weaponsContent += ',AP' + weapon.armorPircing;
        }

        weaponsContent += rosterGuiHandler.abilitiesForDisplay(weapon.abilities, ',');


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


      tableContent += '<tr data-uuid="' + troop.uuid + '" data-troopname="' + troop.name + '">';

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
      tableContent += '<td>' + rosterGuiHandler.abilitiesForDisplay(troop.abilities, '<br />') + '</td>';
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
  },
  /**
   * Gets the correct display format for a weapon range
   * @param weapon
   * @returns {*}
   */
  weaponRangeForDisplay: function (weapon) {

    if (weapon.shootRange === 0) {
      return 'RF';
    } else {
      return 'R' + weapon.shootRange;
    }
  },
  /**
   * Prints a list of abilities and their default value
   * @param abilities
   * @param abSeperator
   * @returns {string}
   */
  abilitiesForDisplay: function (abilities, abSeperator) {
    var abilitiesContent = '';
    var seperator = '';
    $.each(abilities, function (idx, ability) {
      abilitiesContent += seperator;
      abilitiesContent += ability.name;
      if (ability.defaultVal !== 0) {
        abilitiesContent += ' (' + ability.defaultVal + ')';
      }
      seperator = abSeperator;
    });
    return abilitiesContent;
  }
};


$(function () {

  rosterGuiHandler.getAndFillFactions();

  $('#rooster_faction_select').on('change', function () {
    rosterGuiHandler.getAndFillFactionTroopSelect();
  });

  $('#rooster_addTroop_btn').on('click', function () {
    rosterGuiHandler.addSelectedTroopToArmy();
  });


  $('#rooster_troop_edit_save_btn').on('click', function () {
    rosterGuiHandler.saveChangesToTroop();
  });

  $(document).on('click', '.rooster_del_btn', function () {
    var uuid = $(this).closest('tr').attr('data-uuid');
    rosterGuiHandler.removeTroopFromArmy(uuid);
  });


  $(document).on('click', '.rooster_edit_btn', function () {
    var uuid = $(this).closest('tr').data('uuid');
    var troopName = $(this).closest('tr').data('troopname');
    rosterGuiHandler.displayEditPopup(uuid, troopName);
  });

});                                                              