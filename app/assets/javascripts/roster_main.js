var rosterGuiHandler = {

  /**
   * Stores the current troops for the selected faction
   */
  factionTroops: {},

  /**
   * In which order to sort the troop types
   */
  troopTypeSort: ["Leader", "Troop", "Specialist", "Vehicle", "Character"],

  /**
   * Stores the already loaded popover infos vor caching purpose
   */
  popOverInfos: {},


  /**
   * Gets the avaible factions from the backend and fills them into the select
   */
  getAndFillFactions: function() {
    jsRoutes.controllers.RosterController.getFactions().ajax({
      success: function(data) {
        $('#roster_faction_select').html('');

        $.each(data, function(idx, faction) {
          $('#roster_faction_select').append('<option value="' + faction.name + '">' + faction.name + '</option>');
        });

        rosterGuiHandler.getAndFillFactionTroopSelect();
        rosterGuiHandler.getCurrentArmy();
      }
    });
  },

  /**
   * Gets the avaible troops for the selected faction from the backend and displays them in the add troop drop down
   */
  getAndFillFactionTroopSelect: function() {
    var selectedFaction = $('#roster_faction_select').val();
    jsRoutes.controllers.RosterController.getSelectTroopsForFaction(selectedFaction).ajax({
      success: function(data) {

        // store the troops for the faction in the var
        rosterGuiHandler.factionTroops = data;

        var troopTypes = [];
        $.each(data, function(idx) {
          troopTypes.push(idx);
        });

        troopTypes.sort(function(a, b) {
          return rosterGuiHandler.troopTypeSort.indexOf(a) - rosterGuiHandler.troopTypeSort.indexOf(b);
        });

        $('#roster_troop_type_select').html('');
        $.each(troopTypes, function(idx, troopType) {
          $('#roster_troop_type_select').append('<option value="' + troopType + '">' + troopType + '</option>');
        });

        rosterGuiHandler.fillTroopSelectForType();
      }
    });
  },

  /**
   * Fills the troop for the selected type
   */
  fillTroopSelectForType: function() {
    var selectedTroopType = $('#roster_troop_type_select').val();
    var troops = rosterGuiHandler.factionTroops[selectedTroopType];

    $('#roster_addTroop_select').html('');
    $.each(troops, function(idx, troop) {
      $('#roster_addTroop_select').append('<option value="' + troop.name + '" data-image-url="'+troop.imageUrl+'">' + troop.name + ' (' + troop.points + ')</option>');
    });

    rosterGuiHandler.displaySelectedTroopImage();
  },

  /**
   * Displays when avaible the image of the currently selected troop
   */
  displaySelectedTroopImage: function() {
    var imgUrl = $('#roster_addTroop_select :selected').data('imageUrl');

    if(imgUrl !== '') {
      $('#roster_troop_img').attr('src',imgUrl).show();
    } else {
      $('#roster_troop_img').attr('src','').hide();
    }
  },

  /**
   * Adds the selected troop to the army list
   */
  addSelectedTroopToArmy: function() {
    var troopToAdd = {
      faction: $('#roster_faction_select').val(),
      troop: $('#roster_addTroop_select').val()
    };
    jsRoutes.controllers.RosterController.addTroopToArmy().ajax({
      data: JSON.stringify(troopToAdd),
      contentType: "application/json; charset=utf-8",
      success: function(data) {
        rosterGuiHandler.displayCurrentArmyData(data);
      }
    });
  },

  /**
   * Gets the current army as json
   */
  getCurrentArmy: function() {
    jsRoutes.controllers.RosterController.getArmy().ajax({
      success: function(data) {
        rosterGuiHandler.displayCurrentArmyData(data);
      }
    });
  },

  /**
   * Removes the selected troop from the army
   * @param uuid
   */
  removeTroopFromArmy: function(uuid) {
    jsRoutes.controllers.RosterController.removeTroopFromArmy(uuid).ajax({
      success: function(data) {
        rosterGuiHandler.displayCurrentArmyData(data);
      }
    });
  },


  /**
   * Gets needed weapon/item informations from the backend and displays the edit popup
   * @param uuid
   */
  displayEditPopup: function(uuid, troopName) {
    jsRoutes.controllers.RosterController.getWeaponsAndItemsForTroop(uuid).ajax({
      success: function(data) {

        $('#roster_troop_edit_name').text(troopName);
        $('#roster_troop_edit_modal').data('troopuuid', uuid);

        var weaponsContent = '';
        weaponsContent += rosterGuiHandler._displayEditWeaponType('Free', 'free', data);
        weaponsContent += rosterGuiHandler._displayEditWeaponType('Fight', 'fight', data);
        weaponsContent += rosterGuiHandler._displayEditWeaponType('Ranged', 'ranged', data);
        weaponsContent += rosterGuiHandler._displayEditWeaponType('Combo', 'linked', data);
        $('#roster_troop_edit_weapons').html(weaponsContent);

        var itemsContent = '';
        $.each(data.items, function(idx, item) {

          var checked = '';
          for(var iidx in data.troop.items) {
            if(data.troop.items[iidx].name === item.name) {
              checked = 'checked';
              break;
            }
          }

          itemsContent += '<tr>';
          itemsContent += '<td><input class="edit_troop_slected_item" value="' + item.name + '" type="checkbox" ' + checked + ' /></td>';
          itemsContent += '<td>' + item.name + '</td>';
          itemsContent += '<td>' + item.points + '</td>';
          itemsContent += '<td>' + item.rarity + '</td>';
          itemsContent += '</tr>';
        });
        $('#roster_troop_edit_items').html(itemsContent);

        $('#roster_troop_edit_modal').modal('show');
      }
    });
  },

  /**
   * Collects the data for the troop and saves the changes in the backend
   */
  saveChangesToTroop: function() {
    var uuid = $('#roster_troop_edit_modal').data('troopuuid');
    var dataToSave = {
      selectedWeapons: [],
      selectedItems: []
    };

    $.each($('.edit_troop_selected_weapon:checked'), function(idx, obj) {
      dataToSave.selectedWeapons.push($(obj).val());
    });

    $.each($('.edit_troop_slected_item:checked'), function(idx, obj) {
      dataToSave.selectedItems.push($(obj).val());
    });

    jsRoutes.controllers.RosterController.updateTroopWeaponsAndItems(uuid).ajax({
      data: JSON.stringify(dataToSave),
      contentType: "application/json; charset=utf-8",
      success: function(data) {
        rosterGuiHandler.displayCurrentArmyData(data);
        $('#roster_troop_edit_modal').modal('hide');
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
  _displayEditWeaponType: function(headline, type, data) {
    var content = '';

    if(data.weapons[type].length !== 0) {
      content = '<tr class="info"><th colspan="7">' + headline + '</th></tr>';
      $.each(data.weapons[type], function(idx, weapon) {

        var checked = '';
        for(var widx in data.troop.weapons) {
          if(data.troop.weapons[widx].name === weapon.name) {
            checked = 'checked';
            break;
          }
        }

        var tableRow = '<tr>';
        for(var wdidx in data.troop.defaultWeapons) {
          if(data.troop.defaultWeapons[wdidx].name === weapon.name) {
            tableRow = '<tr class="warning">';
            break;
          }
        }

        content += tableRow;
        content += '<td><div class="checkbox"><label>';
        content += '<input data-linked-name="' + weapon.linkedName + '" value="' + weapon.name + '" class="edit_troop_selected_weapon" type="checkbox" ' + checked + '/> ';
        content += weapon.name;
        content += '</label></div></td>';
        content += '<td>' + weapon.points + '</td>';
        content += '<td>' + weapon.victoryPoints + '</td>';
        content += '<td>' + rosterGuiHandler._weaponRangeForDisplay(weapon) + '</td>';
        content += '<td>' + weapon.armorPircing + '</td>';
        content += '<td>' + rosterGuiHandler._abilitiesForDisplay(weapon.abilities, ',') + '</td>';
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

    var disableBtns = armyData.troops.length === 0;
    $('.roster_action_btn').prop('disabled', disableBtns);

    $('#roster_army_points').text(armyData.points);
    $('#roster_army_faction').text(armyData.faction);

    $('#roster_army_name').val(armyData.name);

    $('#roster_troop_tbody').html('');
    var tableContent = '';
    $.each(armyData.troops, function(idx, troop) {

      var weaponsContent = rosterGuiHandler._displayTroopWeapons(troop);

      var reconArmySpecialContent = '';
      if(troop.recon !== 0) {
        reconArmySpecialContent += troop.recon + ' / ' +
          '<u class="infoPopOver helpMouse" data-type="armyspecial" title="' + troop.armySpecial + '">' + troop.armySpecial + '</u>';
      }

      var itemsContent = '';
      $.each(troop.items, function(idx, item) {
        itemsContent += item.name + '<br />';
      });

      tableContent += '<tr data-uuid="' + troop.uuid + '" data-troopname="' + troop.name + '">';
      tableContent += '<td>' + troop.name + '</td>';
      tableContent += '<td>' + troop.modelType.charAt(0) + '</td>';
      tableContent += '<td>' + troop.points + '</td>';
      tableContent += '<td>' + troop.victoryPoints + '</td>';
      tableContent += '<td>' + troop.armour + '</td>';
      tableContent += '<td>' + troop.size + '</td>';
      tableContent += '<td>' + troop.speed + '-' + troop.sprint + '</td>';
      tableContent += '<td>' + rosterGuiHandler._displayStatsValue(troop.shoot) + '</td>';
      tableContent += '<td>' + rosterGuiHandler._displayStatsValue(troop.fight) + '</td>';
      tableContent += '<td>' + rosterGuiHandler._displayStatsValue(troop.survive) + '</td>';
      tableContent += '<td>' + rosterGuiHandler._abilitiesForDisplay(troop.abilities, '<br />') + '</td>';
      tableContent += '<td>' + weaponsContent + '</td>';
      tableContent += '<td>' + itemsContent + '</td>';
      tableContent += '<td>' + reconArmySpecialContent + '</td>';
      tableContent += '<td>';
      tableContent += '<button class="btn btn-info btn-xs roster_edit_btn"><span class="glyphicon glyphicon-pencil"></span></button>';
      tableContent += '<button class="btn btn-info btn-xs roster_clone_btn"><span class="glyphicon glyphicon-plus-sign"></span></button>';
      tableContent += '<button class="btn btn-danger btn-xs roster_del_btn"><span class="glyphicon glyphicon-trash"></span></button>';
      tableContent += '</td>';
      tableContent += '</tr>';
    });

    $('#roster_troop_tbody').html(tableContent);
  },

  /**
   * Generates the html output for the troop weapons
   * @param troop
   * @returns {string}
   * @private
   */
  _displayTroopWeapons: function(troop) {
    var weaponsContent = '';

    $.each(troop.weapons, function(idx, weapon) {
      weaponsContent += '<b>'+weapon.name+'</b>';
      weaponsContent += ' ,' + rosterGuiHandler._weaponRangeForDisplay(weapon);
      if(weapon.armorPircing !== 0) {
        weaponsContent += ', AP' + weapon.armorPircing;
      }
      if(weapon.abilities.length !== 0) {
        weaponsContent += ' ,';
      }
      weaponsContent += rosterGuiHandler._abilitiesForDisplay(weapon.abilities, ',');
      weaponsContent += '<br />';
    });
    
    return weaponsContent;
  },

  /**
   * Generates the stats value to display
   * @param value
   */
  _displayStatsValue: function(value) {
    if(value === 0) {
      return '-';
    }

    return value + '+';
  },

  /**
   * Gets the correct display format for a weapon range
   * @param weapon
   * @returns {*}
   */
  _weaponRangeForDisplay: function(weapon) {
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
  _abilitiesForDisplay: function(abilities, abSeperator) {
    var abilitiesContent = '';
    var seperator = '';
    $.each(abilities, function(idx, ability) {
      abilitiesContent += seperator + '<u class="infoPopOver helpMouse" data-type="ability" title="' + ability.name + '">';
      abilitiesContent += ability.name;
      if(ability.defaultVal !== 0) {
        abilitiesContent += ' (' + ability.defaultVal + ')';
      }
      abilitiesContent += '</u>';
      seperator = abSeperator;
    });
    return abilitiesContent;
  },

  /**
   * Gets the pop over data to display
   * @param type
   * @param key
   */
  getPopoverData: function(type, key, callBack) {
    if(rosterGuiHandler.popOverInfos[type + key] !== undefined) {
      callBack(rosterGuiHandler.popOverInfos[type + key]);
      return;
    }
    jsRoutes.controllers.RosterController.getPopOverData(type, key).ajax({
      success: function(data) {
        rosterGuiHandler.popOverInfos[type + key] = data;
        callBack(data);
      }
    });
  },

  /**
   * Calls the backend to validate the army and displays the result
   */
  validateArmy: function() {
    jsRoutes.controllers.RosterController.validateArmy().ajax({
      success: function(data) {
        var content = '';

        if(data.length === 0) {
          content += 'No errors.';
        } else {
          content += 'Found: ' + data.length + ' errors in army';
          content += '<ul>';
          $.each(data, function(idx, msg) {
            content += '<li>' + msg + '</li>';
          });
          content += '</ul>';
        }

        $('#roster_army_validate_modal_body').html(content);
        $('#roster_army_validate_modal').modal('show');
      }
    });
  },
  /**
   * Clones a troop
   * @param uuid
   */
  cloneTroop: function(uuid) {
    jsRoutes.controllers.RosterController.cloneTroop(uuid).ajax({
      success: function(data) {
        rosterGuiHandler.displayCurrentArmyData(data);
      }
    });
  },
  /**
   * Uploads the selected file and displays the imported army
   */
  importArmy: function() {
    var formData = new FormData();
    formData.append('file', $('#roster_upload_file')[0].files[0]);

    jsRoutes.controllers.RosterController.importArmy().ajax({
      data: formData,
      processData: false,
      contentType: false,
      success: function(data) {
        rosterGuiHandler.displayCurrentArmyData(data);
      },
      error: function(data) {
        alert('Error !!!!');
      }

    });
  },
  /**
   * Changes the name of the army
   */
  changeArmyName: function(newArmyName) {
    jsRoutes.controllers.RosterController.changeArmyName().ajax({
      data: JSON.stringify({'armyName': newArmyName}),
      contentType: "application/json; charset=utf-8",
      success: function(data) {
        rosterGuiHandler.displayCurrentArmyData(data);
      }
    });
  }
};


$(function() {

  rosterGuiHandler.getAndFillFactions();

  $('#roster_faction_select').on('change', function() {
    rosterGuiHandler.getAndFillFactionTroopSelect();
  });

  $('#roster_troop_type_select').on('change', function() {
    rosterGuiHandler.fillTroopSelectForType();
  });

  $('#roster_addTroop_select').on('change',function(){
     rosterGuiHandler.displaySelectedTroopImage();
  });

  $('#roster_addTroop_btn').on('click', function() {
    rosterGuiHandler.addSelectedTroopToArmy();
  });


  $('#roster_troop_edit_save_btn').on('click', function() {
    rosterGuiHandler.saveChangesToTroop();
  });

  $('#roster_validate_army_btn').on('click', function() {
    rosterGuiHandler.validateArmy();
  });

  $('#roster_import_army_btn').on('click', function() {
    rosterGuiHandler.importArmy();
  });

  $('#roster_army_name').on('blur', function() {
    var newName = $(this).val();
    rosterGuiHandler.changeArmyName(newName);
  });

  $(document).on('click', '.roster_del_btn', function() {
    var uuid = $(this).closest('tr').attr('data-uuid');
    rosterGuiHandler.removeTroopFromArmy(uuid);
  });


  $(document).on('click', '.roster_edit_btn', function() {
    var uuid = $(this).closest('tr').data('uuid');
    var troopName = $(this).closest('tr').data('troopname');
    rosterGuiHandler.displayEditPopup(uuid, troopName);
  });

  $(document).on('click', '.edit_troop_selected_weapon', function() {
    var linkedName = $(this).data('linkedName');
    if(linkedName === '') {
      return;
    }

    var thisStatus = $(this).prop('checked');
    $('.edit_troop_selected_weapon[data-linked-name="' + linkedName + '"]').prop('checked', thisStatus);
  });


  $(document).on('click', '.roster_clone_btn', function() {
    var uuid = $(this).closest('tr').data('uuid');
    rosterGuiHandler.cloneTroop(uuid);
  });

  $(document).on('mouseenter', '.infoPopOver', function() {
    var key = $(this).attr('title');
    var type = $(this).data('type');
    $(this).removeClass('infoPopOver');
    var el = $(this);
    rosterGuiHandler.getPopoverData(type, key, function(data) {
      el.popover({content: data, html: true, trigger: 'hover'}).popover('show');
    });
  });

});                                                              