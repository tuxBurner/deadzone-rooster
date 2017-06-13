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
          $('#rooster_addTroop_select').append('<option value="' + troop.name + '">' + troop.modelType + ' | Name: ' + troop.name + ' | Points: ' + troop.points + '</option>');
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
    jsRoutes.controllers.RoosterController.getArmy().ajax({
      success: function(data) {
        roosterGuiHandler.displayCurrentArmyData(data);
      }
    });
  },

  /**
   * Removes the selected troop from the army
   * @param uuid
   */
  removeTroopFromArmy: function(uuid) {
    jsRoutes.controllers.RoosterController.removeTroopFromArmy(uuid).ajax({
      success: function(data) {
        roosterGuiHandler.displayCurrentArmyData(data);
      }
    });
  },


  /**
   * Gets needed weapon/item informations from the backend and displays the edit popup
   * @param uuid
   */
  displayEditPopup: function(uuid,troopName) {
    jsRoutes.controllers.RoosterController.getWeaponsAndItemsForTroop(uuid).ajax({
      success: function(data) {

        $('#rooster_troop_edit_name').text(troopName);

        var weaponsContent = '';
        weaponsContent += roosterGuiHandler.displayEditWeaponType('Free','free',data.weapons);
        weaponsContent += roosterGuiHandler.displayEditWeaponType('Fight','fight',data.weapons);
        weaponsContent += roosterGuiHandler.displayEditWeaponType('Ranged','ranged',data.weapons);
        $('#rooster_troop_edit_weapons').html(weaponsContent);

        var itemsContent = '';
        $.each(data.items,function(idx, item) {
          itemsContent += '<tr>';
          itemsContent += '<td>'+item.name+'</td>';
          itemsContent += '<td>'+item.points+'</td>';
          itemsContent += '<td>'+item.rarity+'</td>';
          itemsContent += '</tr>';
        });
        $('#rooster_troop_edit_items').html(itemsContent);

        $('#rooster_troop_edit_modal').modal('show');
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
  displayEditWeaponType: function(headline, type, data) {
    var content = '';

    if(data[type].length !== 0) {
      content = '<tr class="info"><th colspan="6">'+headline+'</th></tr>';
      $.each(data[type], function(idx, weapon) {
        content += '<tr>';
        content += '<td>' + weapon.name + '</td>';
        content += '<td>' + weapon.points + '</td>';
        content += '<td>' + weapon.victoryPoints + '</td>';
        content += '<td>' + roosterGuiHandler.weaponRangeForDisplay(weapon) + '</td>';
        content += '<td>' + weapon.armorPircing + '</td>';
        content += '<td>' + roosterGuiHandler.abilitiesForDisplay(weapon.abilities, ',') + '</td>';
        content += '</tr>';
      });
    }
    return content;
  },

  /**
   * Populates the army table with the current data
   * @param armyData
   */
  displayCurrentArmyData: function(armyData) {

    $('#rooster_army_points').text(armyData.points);
    $('#rooster_army_faction').text(armyData.faction);

    $('#rooster_faction_select').attr('disabled', armyData.faction !== '');

    $('#rooster_troop_tbody').html('');
    var tableContent = '';
    $.each(armyData.troops, function(idx, troop) {

      var weaponsContent = '';
      $.each(troop.weapons, function(idx, weapon) {
        weaponsContent += weapon.name;
        weaponsContent += '<span class="badge">' + roosterGuiHandler.weaponRangeForDisplay(weapon) + '</span>';

        if(weapon.armorPircing !== 0) {
          weaponsContent += ',AP' + weapon.armorPircing;
        }

        weaponsContent += roosterGuiHandler.abilitiesForDisplay(weapon.abilities, ',');


        weaponsContent += '<br />';
      });

      var reconArmySpecialContent = '';
      if(troop.recon !== 0) {
        reconArmySpecialContent += troop.recon + '+ / ' + troop.armySpecial;
      }

      var itemsContent = '';
      $.each(troop.items, function(idx, item) {
        itemsContent += item.name + '<br />';
      });


      tableContent += '<tr data-uuid="' + troop.uuid + '" data-troopname="'+troop.name+'">';

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
      tableContent += '<td>' + roosterGuiHandler.abilitiesForDisplay(troop.abilities, '<br />') + '</td>';
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
  weaponRangeForDisplay: function(weapon) {

    if(weapon.shootRange === 0) {
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
  abilitiesForDisplay: function(abilities, abSeperator) {
    var abilitiesContent = '';
    var seperator = '';
    $.each(abilities, function(idx, ability) {
      abilitiesContent += seperator;
      abilitiesContent += ability.name;
      if(ability.defaultVal !== 0) {
        abilitiesContent += ' (' + ability.defaultVal + ')';
      }
      seperator = abSeperator;
    });
    return abilitiesContent;
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

  $(document).on('click', '.rooster_del_btn', function() {
    var uuid = $(this).closest('tr').attr('data-uuid');
    roosterGuiHandler.removeTroopFromArmy(uuid);
  });

  $(document).on('click', '.rooster_edit_btn', function() {
    var uuid = $(this).closest('tr').data('uuid');
    var troopName = $(this).closest('tr').data('troopname');
    roosterGuiHandler.displayEditPopup(uuid,troopName);
  });

});                                                              