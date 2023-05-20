import {JobState} from "./types";
import {createSlice} from "@reduxjs/toolkit";
import {changeJobState, exceptions, getJobs, jobDelete, saveJob} from "./thunks";
import {useInjectReducer} from "utils/@reduxjs/injectReducer";

export const initialState: JobState = {
  jobs: [],
  jobExceptions: [],
  jobListLoading: false,
  jobSaveLoading: false,
  jobDeleteLoading: false,
  jobLogsLoading: false,
  jobUpdatingLoading: false,
};

const slice = createSlice({
  name: 'job',
  initialState,
  reducers: {},
  extraReducers: builder => {

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Jobs List.
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    builder.addCase(getJobs.pending, state => {
      state.jobListLoading = true;
    });
    builder.addCase(getJobs.fulfilled, (state, action) => {
      state.jobListLoading = false;
      state.jobs = action.payload.map(value => ({ ...value }));
    });
    builder.addCase(getJobs.rejected, state => {
      state.jobListLoading = false;
    })

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Jobs Save.
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    builder.addCase(saveJob.pending, state => {
      state.jobSaveLoading = true;
    });
    builder.addCase(saveJob.fulfilled, (state, action) => {
      state.jobSaveLoading = false;
    });
    builder.addCase(saveJob.rejected, state => {
      state.jobSaveLoading = false;
    })


    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Jobs Log.
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    builder.addCase(exceptions.pending, state => {

    })
    builder.addCase(exceptions.fulfilled, (state, action) => {
      state.jobExceptions = action.payload.map(_ => _);
    })
    builder.addCase(exceptions.rejected, state => {

    })

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Jobs State.
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    builder.addCase(changeJobState.pending, state => {
      state.jobUpdatingLoading = true;
    })
    builder.addCase(changeJobState.fulfilled, state => {
      state.jobUpdatingLoading = false;
    })
    builder.addCase(changeJobState.rejected, state => {
      state.jobUpdatingLoading = false;
    })

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Jobs Delete.
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    builder.addCase(jobDelete.pending, state => {
      state.jobDeleteLoading = true;
    });
    builder.addCase(jobDelete.fulfilled, (state, action) => {
      state.jobDeleteLoading = false;
    });
    builder.addCase(jobDelete.rejected, state => {
      state.jobDeleteLoading = false;
    })
  }
});

export const { actions: jobActions, reducer } = slice;

export const useJobSlice = () => {
  useInjectReducer({ key: slice.name, reducer: slice.reducer });
  return { actions: slice.actions };
}
