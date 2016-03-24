import Ember from 'ember';

export default Ember.Object.extend({
  email: null,
  gender: null,
  birthDate: null,
  countries: [],
  countryNames: Ember.computed('countries.[]', function() {
    return this.get('countries').mapBy('full_name');
  }),
  packingListItems: Ember.computed('countries.[]', function() {
    var packingListItems = [];
    this.get('countries').forEach(function(c) {
      //c.packing
      packingListItems.pushObjects(c.get('items'));
    });

    return this.uniqByName(packingListItems);
  }),
  vaccines: Ember.computed('countries.[]', function() {
    var vaccines = [];
    this.get('countries').forEach(function(c) {
      //c.packing
      vaccines.pushObjects(c.get('vaccines'));
    });

    return this.uniqByName(vaccines);
  }),
  uniqByName: function(items) {
    var names = [];
    var uniqItems = []

    items.forEach(function(item) {
        if(!names.contains(item.get('name'))) {
          names.pushObject(item.get('name'));
          uniqItems.pushObject(item);
        }
    });

    return uniqItems;
  }
});