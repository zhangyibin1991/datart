export interface JobState {

  // Tasks data.
  jobs: JobViewModel[];

  jobExceptions: string[];

  // List tasks?
  jobListLoading: boolean;

  // Saving tasks?
  jobSaveLoading: boolean;

  // Deleting tasks?
  jobDeleteLoading: boolean;

  jobLogsLoading: boolean;

  jobUpdatingLoading: boolean;
}

export interface Job {
  id: string
  name: string
  parallelism?: number
  state: string
  startTime: number
  createBy: String
  createTime: string
  updateBy: string
  updateTime: string
  args?: string
  sql: string
  description?: string
  orgId: String
}

export interface JobViewModel extends Job {

}

export interface AddJobParams {
  job: Omit<Job, 'id'>;
  resolve: () => void;
}
export interface JobExHistory {
  entries?: [];
  truncated?: boolean;
}
// export interface JobExceptionsModel {
//
//   exceptionHistory?: JobExHistory;
//   rootException?: string;
//   timestamp?: number;
// }

export interface JobLogParam {
  job: Job;
  resolve: () => void;
}

export interface JobStateParam {
  job: Job;
  state: string;
  resolve: () => void;
}

export interface JobDeleteParam {
  job: Job;
  resolve: () => void;
}

