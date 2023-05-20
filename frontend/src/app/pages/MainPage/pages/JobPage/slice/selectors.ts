import {createSelector} from '@reduxjs/toolkit';
import {RootState} from 'types';
import {initialState} from '.';

const selectDomain = (state: RootState) => state.job || initialState;

export const selectJobs = createSelector(
  [selectDomain],
  state => state.jobs
)

export const selectJobException = createSelector(
  [selectDomain],
  state => state.jobExceptions
)

export const selectJobListLoading = createSelector(
  [selectDomain],
  state => state.jobListLoading,
);

export const selectJobSaveLoading = createSelector(
  [selectDomain],
  state => state.jobSaveLoading
)

export const selectJobDeleteLoading = createSelector(
  [selectDomain],
  state => state.jobDeleteLoading
)

export const selectJobLogsLoading = createSelector(
  [selectDomain],
  state => state.jobLogsLoading
)

export const selectJobUpdateLoading = createSelector(
  [selectDomain],
  state => state.jobUpdatingLoading
)

