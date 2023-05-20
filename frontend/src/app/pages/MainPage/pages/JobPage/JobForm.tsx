import {memo, useCallback, useRef, useState} from "react";
import AceEditor from 'react-ace';
import 'ace-builds/src-noconflict/mode-sql';
import 'ace-builds/src-noconflict/theme-github';
import {ModalForm, ModalFormProps} from 'app/components';
import {DateFormat} from "app/constants";
import {Form, FormInstance, Input} from "antd";
import {JobFormModel} from "./types";
import {Job} from "./slice/types";
import {CommonFormTypes} from "globalConstants";

const { TextArea } = Input;

interface JobFormProps extends ModalFormProps {
  orgId: string;
  type: undefined | CommonFormTypes;
  job: undefined | Job
  onSave: (values) => void;
}

export const JobForm = memo(
  ({
    orgId
    , type
    , job // Task's detail
    , visible
    , onSave
    , afterClose
    , ...modalProps
  } : JobFormProps) => {

    const formRef = useRef<FormInstance<JobFormModel>>();
    const [dateFormat, setDateFormat] = useState<DateFormat | undefined>();

    // save callback.
    const save = useCallback(
      values => {
        onSave({...values, name: values.name, dateFormat})
      }
      , [onSave, dateFormat]
    )

    const onAfterClose = useCallback(() => {
      // setType(
      //   scope === VariableScopes.Public
      //     ? VariableTypes.Permission
      //     : VariableTypes.Query,
      // );
      // setValueType(VariableValueTypes.String);
      // setExpression(false);
      afterClose && afterClose();
    }, [afterClose]);

    return (
      <ModalForm {...modalProps}
                 formProps={{
                   labelAlign: 'left',
                   labelCol: { offset: 1, span: 3 },
                   wrapperCol: { span: 19 },
                   className: '',
                 }}
                 type       = { type }
                 width      = { 800 }
                 visible    = { visible }
                 onSave     = { save }
                 afterClose = { afterClose }
                 ref        = { formRef }
                 destroyOnClose
      >
        <Form.Item name = 'jobName' label = '作业名称'>
          <Input />
        </Form.Item>
        <Form.Item name = 'parallelism' label = '并行度'>
          <Input />
        </Form.Item>
        {/*<Form.Item name='mode2' label='运行模式'>*/}
        {/*  <Radio.Group value='LOCAL' defaultValue='LOCAL'>*/}
        {/*    <Radio.Button value="LOCAL">Local</Radio.Button>*/}
        {/*    <Radio.Button value="YARN">Yarn</Radio.Button>*/}
        {/*  </Radio.Group>*/}
        {/*</Form.Item>*/}
        {/*<Form.Item name = 'args' label = '运行参数'>*/}
        {/*  <Input />*/}
        {/*</Form.Item>*/}
        <Form.Item name = 'sql' label = 'SQL脚本'>
          <AceEditor
            mode="sql"
            theme="github"
            name="my-editor"
            editorProps={{ $blockScrolling: true }}
            width="100%"
            height="500px"
          />
        </Form.Item>
        <Form.Item name = 'description' label = '任务描述'>
          <TextArea rows={4} />
        </Form.Item>
      </ModalForm>
    )
  }
)
