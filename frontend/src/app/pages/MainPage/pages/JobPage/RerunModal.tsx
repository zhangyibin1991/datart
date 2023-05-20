import {Modal, ModalProps} from "antd";
import {Job} from "./slice/types";
import {memo} from "react";
import {ExclamationCircleOutlined} from "@ant-design/icons";

interface RerunModalProps extends ModalProps {
  visible: boolean;
  job: undefined | Job;
}

export const RerunModal = memo(
  ({
    visible
    , job
    , ...modalProps
  } : RerunModalProps) => {

    return(
      <Modal title = { <span><ExclamationCircleOutlined style={{ marginRight: 8 }}/>重新执行</span>}
             visible = { visible }
             {...modalProps}
      >
        是否确认重新提交执行&lt;{job?.name}&gt;?
      </Modal>
    )
  }
)
