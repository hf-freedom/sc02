import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

export default new Vuex.Store({
  state: {
    currentTask: null,
    currentTemplateType: null,
    templates: []
  },
  getters: {
    currentTask: state => state.currentTask,
    currentTemplateType: state => state.currentTemplateType,
    templates: state => state.templates,
    hasErrors: state => {
      if (!state.currentTask || !state.currentTask.dataRows) {
        return false
      }
      return state.currentTask.dataRows.some(row => !row.valid)
    },
    validCount: state => {
      if (!state.currentTask || !state.currentTask.dataRows) {
        return 0
      }
      return state.currentTask.dataRows.filter(row => row.valid).length
    },
    invalidCount: state => {
      if (!state.currentTask || !state.currentTask.dataRows) {
        return 0
      }
      return state.currentTask.dataRows.filter(row => !row.valid).length
    }
  },
  mutations: {
    SET_CURRENT_TASK(state, task) {
      state.currentTask = task
    },
    SET_CURRENT_TEMPLATE_TYPE(state, type) {
      state.currentTemplateType = type
    },
    SET_TEMPLATES(state, templates) {
      state.templates = templates
    },
    UPDATE_ROW(state, { rowIndex, row }) {
      if (state.currentTask && state.currentTask.dataRows) {
        Vue.set(state.currentTask.dataRows, rowIndex, row)
      }
    },
    DELETE_ROW(state, rowIndex) {
      if (state.currentTask && state.currentTask.dataRows) {
        state.currentTask.dataRows.splice(rowIndex, 1)
        state.currentTask.dataRows.forEach((row, index) => {
          row.rowIndex = index
        })
      }
    },
    ADD_ROW(state, row) {
      if (state.currentTask && state.currentTask.dataRows) {
        row.rowIndex = state.currentTask.dataRows.length
        state.currentTask.dataRows.push(row)
      }
    },
    CLEAR_TASK(state) {
      state.currentTask = null
    }
  },
  actions: {
    setCurrentTask({ commit }, task) {
      commit('SET_CURRENT_TASK', task)
    },
    setCurrentTemplateType({ commit }, type) {
      commit('SET_CURRENT_TEMPLATE_TYPE', type)
    },
    setTemplates({ commit }, templates) {
      commit('SET_TEMPLATES', templates)
    },
    updateRow({ commit }, payload) {
      commit('UPDATE_ROW', payload)
    },
    deleteRow({ commit }, rowIndex) {
      commit('DELETE_ROW', rowIndex)
    },
    addRow({ commit }, row) {
      commit('ADD_ROW', row)
    },
    clearTask({ commit }) {
      commit('CLEAR_TASK')
    }
  },
  modules: {
  }
})
