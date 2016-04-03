import Ember from 'ember';

export default Ember.Component.extend({
  tagName: 'li',
  classNames:'list-group-item',
  vaccine: null,
  vaccineDate: null,
  actions: {
    selectVaccine: function() {
      if(!this.get('vaccine').get('vaccinated')) {
        this.set('savingVaccine', true)
        this.set('vaccinated', !this.get('vaccinated'));
      }
    },
    cancel: function() {
      this.set('savingVaccine', false)
      this.set('vaccinated', !this.get('vaccinated'));
    },
    save: function() {
      this.set('savingVaccine', false)
      this.sendAction('save', this.get('vaccine'), this.get('vaccineDate'));
    }
  }

});
