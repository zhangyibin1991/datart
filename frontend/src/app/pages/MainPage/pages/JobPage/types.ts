import {Job} from './slice/types';

export interface JobFormModel
  extends Omit<Job, 'id' | 'defaultValue'> {
  defaultValue: any[];
}
