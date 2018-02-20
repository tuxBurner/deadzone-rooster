let rosterGuiHandler = {

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
    let selectedFaction = $('#roster_faction_select').val();
    jsRoutes.controllers.RosterController.getSelectTroopsForFaction(selectedFaction).ajax({
      success: function(data) {

        // store the troops for the faction in the var
        rosterGuiHandler.factionTroops = data;

        let troopTypes = [];
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
    let selectedTroopType = $('#roster_troop_type_select').val();
    let troops = rosterGuiHandler.factionTroops[selectedTroopType];

    $('#roster_addTroop_select').html('');
    $.each(troops, function(idx, troop) {
      $('#roster_addTroop_select').append('<option value="' + troop.name + '" data-image-url="' + troop.imageUrl + '">' + troop.name + ' (' + troop.points + ')</option>');
    });

    rosterGuiHandler.displaySelectedTroopImage();
  },

  /**
   * Displays when avaible the image of the currently selected troop
   */
  displaySelectedTroopImage: function() {
    let imgUrl = $('#roster_addTroop_select :selected').data('imageUrl');

    if(imgUrl !== '') {
      $('#roster_troop_img').attr('src', imgUrl).show();
    } else {
      $('#roster_troop_img').attr('src', '').hide();
    }
  },

  /**
   * Adds the selected troop to the army list
   */
  addSelectedTroopToArmy: function() {
    let troopToAdd = {
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


        let weaponsContent = rosterGuiHandler._displayEditWeaponType('Free', 'free', data);
        weaponsContent += rosterGuiHandler._displayEditWeaponType('Fight', 'fight', data);
        weaponsContent += rosterGuiHandler._displayEditWeaponType('Ranged', 'ranged', data);
        weaponsContent += rosterGuiHandler._displayEditWeaponType('Combo', 'linked', data);
        $('#roster_troop_edit_weapons').html(weaponsContent);

        let itemsContent = '';

        data.items.forEach(item => {
          let checked = '';
          for(var iidx in data.troop.items) {
            if(data.troop.items[iidx].name === item.name) {
              checked = 'checked';
              break;
            }
          }

          itemsContent += `<tr>
                            <td>
                              <div class="checkbox">
                                <label> 
                                  <input class="edit_troop_slected_item" value="${item.name}" type="checkbox" ${checked} />
                                  ${item.name}
                                </label>
                              </div>
                            </td>
                            <td>${item.points}</td>
                            <td>${item.rarity}</td>       
                          </tr>`;
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
    let uuid = $('#roster_troop_edit_modal').data('troopuuid');
    let dataToSave = {
      selectedWeapons: [],
      selectedItems: []
    };

    $.each($('.edit_troop_selected_weapon:checked'), (idx, obj) => {
      dataToSave.selectedWeapons.push($(obj).val());
    });

    $.each($('.edit_troop_slected_item:checked'), (idx, obj) => {
      dataToSave.selectedItems.push($(obj).val());
    });

    jsRoutes.controllers.RosterController.updateTroopWeaponsAndItems(uuid).ajax({
      data: JSON.stringify(dataToSave),
      contentType: "application/json; charset=utf-8",
      success: (data) => {
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
    let content = '';

    if(data.weapons[type].length !== 0) {

      content = `<tr class="info">
                   <th colspan="7">${headline}</th>
                 </tr>`;

      data.weapons[type].forEach(weapon => {
        let checked = '';
        for(let widx in data.troop.weapons) {
          if(data.troop.weapons[widx].name === weapon.name) {
            checked = 'checked';
            break;
          }
        }


        let tableRowClass = '';
        for(let wdidx in data.troop.defaultWeapons) {
          if(data.troop.defaultWeapons[wdidx].name === weapon.name) {
            tableRowClass = 'warning';
            break;
          }
        }
        content += `<tr class="${tableRowClass}">
                      <td>
                        <div class="checkbox">
                          <label>
                            <input data-linked-name="${weapon.linkedName}" value="${weapon.name}" class="edit_troop_selected_weapon" type="checkbox" ${checked}/>
                           ${weapon.name}
                          </label>                          
                        </div> 
                      </td>
                      <td>${weapon.points}</td>
                      <td>${weapon.victoryPoints}</td>
                      <td>${rosterGuiHandler._weaponRangeForDisplay(weapon)}</td>
                      <td>${weapon.armorPircing}</td>
                      <td>${rosterGuiHandler._abilitiesForDisplay(weapon.abilities, ',')}</td>
                      <td>${weapon.hardPoints}</td>
                    </tr>`;


      });
    }
    return content;
  },

  /**
   * Populates the army table with the current data
   * @param armyData
   */
  displayCurrentArmyData: function(armyData) {

    let disableBtns = armyData.troopsWithAmount.length === 0;
    $('.roster_action_btn').prop('disabled', disableBtns);

    $('#roster_army_points').text(armyData.points);
    $('#roster_army_faction').text(armyData.faction);

    $('#roster_army_name').val(armyData.name);

    $('#roster_troop_tbody').html('');
    let tableContent = '';

    let troopsByType = {};

    armyData.troopsWithAmount.forEach(troopWithAmount => {
      let modelType = troopWithAmount.troop.modelType;
      if(troopsByType[modelType] === undefined) {
        troopsByType[modelType] = [];
      }

      troopsByType[modelType].push(troopWithAmount);
    });


    Object.keys(troopsByType).forEach(modelType => {

      tableContent += `<tr class="info"><th colspan="17"><h5>${modelType}</h5></th></tr>`;

      // sort the troops by name
      troopsByType[modelType].sort((a, b) => {
        if(a.troop.name <= b.troop.name) {
          return -1;
        }

        if(a.troop.name >= b.troop.name) {
          return 1;
        }

        return 0;
      });

      // iterate over the troops and
      troopsByType[modelType].forEach(amountTroop => {
        let troop = amountTroop.troop;

        let reconArmySpecialContent = (troop.recon !== 0) ? `${troop.recon} / <u class="infoPopOver helpMouse" data-type="armyspecial" title="${troop.armySpecial}">${troop.armySpecial}</u>` : '';

        let itemsContent = '';
        troop.items.forEach(item => {
          itemsContent += `<u class="infoPopOver helpMouse" data-type="item" title="${item.name}">${item.name}</u><br />`;
        });

        tableContent += `<tr  data-uuid="${troop.uuid}" data-troopname="${troop.name}">
                           <td>${troop.name}</td>
                           <td>${troop.modelType.charAt(0)}</td>
                           <td>${troop.baseStats.points}</td>
                           <td>${troop.baseStats.victoryPoints}</td>
                           <td>${troop.baseStats.armour}</td>
                           <td>${troop.baseStats.size}</td>
                           <td>${troop.baseStats.speed}-${troop.baseStats.sprint}</td>
                           <td>${rosterGuiHandler._displayStatsValue(troop.baseStats.shoot)}</td>
                           <td>${rosterGuiHandler._displayStatsValue(troop.baseStats.fight)}</td>
                           <td>${rosterGuiHandler._displayStatsValue(troop.baseStats.survive)}</td>
                           <td>${rosterGuiHandler._abilitiesForDisplay(troop.abilities, '<br />')}</td>
                           <td>${rosterGuiHandler._displayTroopWeapons(troop)}</td>
                           <td>${troop.baseStats.hardPoints}</td>
                           <td>${itemsContent}</td>
                           <td>${reconArmySpecialContent}</td>
                           <td>
                             <input class="form-control roster_amount_input" type="number" value="${amountTroop.amount}" min="1" style="width: 80px;"/>
                           </td>
                           <td>
                             <div class="btn-group btn-group-xs">
                                <button class="btn btn-info roster_edit_btn"><span class="glyphicon glyphicon-pencil"></span></button>
                                <button class="btn btn-info roster_clone_btn"><span class="glyphicon glyphicon-plus-sign"></span></button>
                                <button class="btn btn-danger roster_del_btn"><span class="glyphicon glyphicon-trash"></span></button>
                             </div> 
                           </td>
                         </tr>`;


      });

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
    let weaponsContent = '';

    troop.weapons.forEach(weapon => {
      let apContent = (weapon.armorPircing !== 0) ? `, AP ${weapon.armorPircing}` : '';
      let hpContent = (weapon.hardPoints !== 0) ? `, HP ${weapon.hardPoints}` : '';
      let abSep = (weapon.abilities.length !== 0) ? ' ,' : '';
      weaponsContent += `<b>${weapon.name}</b>, ${rosterGuiHandler._weaponRangeForDisplay(weapon)} ${apContent} ${hpContent} ${abSep} ${rosterGuiHandler._abilitiesForDisplay(weapon.abilities, ',')} <br />`;
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

    return `${value}+`;
  },

  /**
   * Gets the correct display format for a weapon range
   * @param weapon
   * @returns {*}
   */
  _weaponRangeForDisplay: function(weapon) {
    if(weapon.shootRange === 0) {
      return `RF`;
    } else {
      return `R${weapon.shootRange}`;
    }
  },
  /**
   * Prints a list of abilities and their default value
   * @param abilities
   * @param abSeperator
   * @returns {string}
   */
  _abilitiesForDisplay: function(abilities, abSeperator) {
    let abilitiesContent = '';
    let seperator = '';

    abilities.forEach(ability => {
      let abilityDefaultVal = (ability.defaultVal !== 0) ? ` (${ability.defaultVal})` : '';
      abilitiesContent += `${seperator} <u class="infoPopOver helpMouse" data-type="ability" title="${ability.name}">${ability.name} ${abilityDefaultVal}</u>`;
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
      success: (data) => {
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
      success: (data) => {
        var content = '';

        if(data.length === 0) {
          content += 'No errors.';
        } else {
          content += 'Found: ' + data.length + ' errors in army';

          let msgContent = '';
          data.forEach(msg => {
            msgContent += `<li>${msg}</li>`;
          });


          content += `<ul>${data.map(msg => `<li>${msg}</li>`).join('')}</ul>`;

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
      success: (data) => {
        rosterGuiHandler.displayCurrentArmyData(data);
      }
    });
  },
  /**
   * Uploads the selected file and displays the imported army
   */
  importArmy: function() {
    let formData = new FormData();
    formData.append('file', $('#roster_upload_file')[0].files[0]);

    jsRoutes.controllers.RosterController.importArmy().ajax({
      data: formData,
      processData: false,
      contentType: false,
      success: (data) => {
        rosterGuiHandler.displayCurrentArmyData(data);
        $('#roster_army_import_modal').modal('hide');
      },
      error: (data) => {
        alert(`Error !!!! ${data}`);
      }

    });
  },
  /**
   * Changes the name of the army
   */
  changeArmyName: function(newArmyName) {
    jsRoutes.controllers.RosterController.changeArmyName().ajax({
      data: JSON.stringify({armyName: newArmyName}),
      contentType: "application/json; charset=utf-8",
      success: (data) => {
        rosterGuiHandler.displayCurrentArmyData(data);
      }
    });
  },

  /**
   * Changes the amount of a troop
   * @param uuid the uuid of the troop
   * @param newAmount the amount to set on the troop
   */
  changeTroopAmount: function(uuid, newAmount) {
    jsRoutes.controllers.RosterController.changeAmountOfTroop(uuid).ajax({
      data: JSON.stringify({amount: newAmount}),
      contentType: "application/json; charset=utf-8",
      success: (data) => {
        rosterGuiHandler.displayCurrentArmyData(data);
      }
    });
  }
};


$(function() {

  rosterGuiHandler.getAndFillFactions();

  $('#roster_faction_select').on('change', () => rosterGuiHandler.getAndFillFactionTroopSelect());

  $('#roster_troop_type_select').on('change', () => rosterGuiHandler.fillTroopSelectForType());

  $('#roster_addTroop_select').on('change', () => rosterGuiHandler.displaySelectedTroopImage());

  $('#roster_addTroop_btn').on('click', () => rosterGuiHandler.addSelectedTroopToArmy());


  $('#roster_troop_edit_save_btn').on('click', () => rosterGuiHandler.saveChangesToTroop());

  $('#roster_validate_army_btn').on('click', () => rosterGuiHandler.validateArmy());

  $('#roster_open_import_army_btn').on('click', () => $('#roster_army_import_modal').modal('show'));

  $('#roster_import_army_btn').on('click', () => rosterGuiHandler.importArmy());

  $('#roster_army_name').on('blur', function() {
    let newName = $(this).val();
    rosterGuiHandler.changeArmyName(newName);
  });

  $(document).on('click', '.roster_del_btn', function() {
    let uuid = $(this).closest('tr').attr('data-uuid');
    rosterGuiHandler.removeTroopFromArmy(uuid);
  });

  $(document).on('change', '.roster_amount_input', function() {
    let uuid = $(this).closest('tr').data('uuid');
    let amount = $(this).val();
    rosterGuiHandler.changeTroopAmount(uuid, amount);

  });

  $(document).on('click', '.roster_edit_btn', function() {
    let uuid = $(this).closest('tr').data('uuid');
    let troopName = $(this).closest('tr').data('troopname');
    rosterGuiHandler.displayEditPopup(uuid, troopName);
  });

  $(document).on('click', '.edit_troop_selected_weapon', function() {
    let linkedName = $(this).data('linkedName');
    if(linkedName === '') {
      return;
    }

    let thisStatus = $(this).prop('checked');
    $('.edit_troop_selected_weapon[data-linked-name="' + linkedName + '"]').prop('checked', thisStatus);
  });


  $(document).on('click', '.roster_clone_btn', function() {
    let uuid = $(this).closest('tr').data('uuid');
    rosterGuiHandler.cloneTroop(uuid);
  });

  $(document).on('mouseenter', '.infoPopOver', function() {
    let key = $(this).attr('title');
    let type = $(this).data('type');
    $(this).removeClass('infoPopOver');
    let el = $(this);
    rosterGuiHandler.getPopoverData(type, key, function(data) {
      el.popover({content: data, html: true, trigger: 'hover', placement: 'auto'}).popover('show');
    });
  });

});                                                              