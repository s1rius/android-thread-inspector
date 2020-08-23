import React from 'react';
import {
  FlipperBasePlugin, 
  FlipperPlugin, 
  FlipperDevicePlugin,
  View, 
  styled,
  Text,
  Button,
  ContextMenu,
  FlexColumn,
  SearchableTable,
  ManagedTable,
  KeyboardActions,
  TableBodyRow,
  TableColumnOrder,
  TableColumnSizes,
  TableColumns,
  BaseAction,
  BaseDevice,
  ManagedTableClass,
  Props as PluginProps
 } from 'flipper';

 import {MenuTemplate} from 'flipper/src/ui/components/ContextMenu';
 import dateFormat from 'dateformat';

type S1Thread = {
  id: string;
  name: string;
  group: string;
  state: string;
  priority: number;
  daemon: string;
  alive: String;
  createAt: number;
};

type Entries = ReadonlyArray<{
  readonly row: TableBodyRow;
  readonly entry: S1Thread;
}>;

type Message = {
  newThread: S1Thread;
  threads: Array<S1Thread>;
}

type State = {
  readonly rows: ReadonlyArray<TableBodyRow>;
  readonly entries: Entries;
};

type PersistedState = {
  rows: Array<TableBodyRow>;
  threads: Array<S1Thread>;
};

const COLUMNS = {
  id: {
    value: 'ID',
  },
  name: {
    value: 'NAME',
  },
  group: {
    value: 'GROUP',
  },
  time: {
    value: 'CREATE-TIME'
  },
  state: {
    value: 'STATE',
  },
  priority: {
    value: 'PRIORITY',
  },
  daemon: {
    value: 'DAEMON',
  }
} as const;

const COLUMN_SIZE = {
  id: 100,
  name: 240,
  group: 120,
  time: 'flex',
  state: 100,
  priority: 80,
  daemon: 80,
} as const;

const INITIAL_COLUMN_ORDER = [
  {
    key: 'id',
    visible: true,
  },
  {
    key: 'name',
    visible: true,
  },
  {
    key: 'group',
    visible: true,
  },
  {
    key: 'time',
    visible: true,
  },
  {
    key: 'state',
    visible: true,
  },
  {
    key: 'priority',
    visible: true,
  },
  {
    key: 'daemon',
    visible: true,
  },
];

const HiddenScrollText = styled(Text)({
  alignSelf: 'baseline',
  userSelect: 'none',
  lineHeight: '130%',
  marginTop: 5,
  paddingBottom: 3,
  '&::-webkit-scrollbar': {
    display: 'none',
  },
});

export default class S1ThreadTable extends FlipperPlugin <
  State,
  BaseAction,
  PersistedState
  > {

  static defaultPersistedState = {
    rows:[],
    threads:[],
  }

  static supportsDevice(device: BaseDevice) {
    return (
      device.os === 'Android'
    );
  }

  tableRef: ManagedTableClass | undefined;
  columns: TableColumns;
  columnSizes: TableColumnSizes;
  columnOrder: TableColumnOrder;
  
  constructor(props: PluginProps<PersistedState>) {
    super(props);
    this.columns = COLUMNS
    this.columnSizes = COLUMN_SIZE
    this.columnOrder = INITIAL_COLUMN_ORDER
  }

  static ContextMenu = styled(ContextMenu)({
    flex: 1,
  });

  buildContextMenuItems: () => MenuTemplate = () => [
    {
      type: 'separator',
    },
    {
      label: 'Clear all',
      click: this.refreshAll,
    },
  ];
  
  setTableRef = (ref: ManagedTableClass) => {
    this.tableRef = ref;
  };

  refreshAll(){
    //todo
  }

  /*
   * Reducer to process incoming "send" messages from the mobile counterpart.
   */
  static persistedStateReducer(
    persistedState: PersistedState,
    method: string,
    payload: Message,
  ) {
    if (method === 'refreshAll') {
      return Object.assign({}, persistedState, {
        threads: persistedState.threads.concat(payload.threads),
        rows: persistedState.rows.concat(processThreads(payload.threads)).sort((a, b)=> a.createAt - b.createAt)
      });
    }
    if (method === 'newThread') {
      return Object.assign({}, persistedState, {
        threads: persistedState.threads.concat([payload.newThread]),
        rows: persistedState.rows.concat(processThread(payload.newThread)).sort((a, b)=> a.createAt - b.createAt)
      });
    }
    if (method === 'updateThread') {
      let updateThread = payload.newThread
      persistedState.threads.forEach((item, index) => {
          if (item.id === updateThread.id) {
            item.state = updateThread.state
          }
      })
      persistedState.rows.forEach((row, index, object) => {
        if (row.key === updateThread.id) {
          object.splice(index, 1)
        }
      })
      return Object.assign({}, persistedState, {
        threads: persistedState.threads,
        rows: persistedState.rows.concat(processThread(updateThread)).sort((a, b)=> a.createAt - b.createAt)
      })
    }
    return persistedState;
  }

  render() {
    const {persistedState} = this.props

    return (
      <S1ThreadTable.ContextMenu
        buildItems={this.buildContextMenuItems}
        component={FlexColumn}>
        <ManagedTable
          innerRef={this.setTableRef}
          floating={false}
          multiline={true}
          columnSizes={this.columnSizes}
          columnOrder={this.columnOrder}
          columns={this.columns}
          rows={persistedState.rows}
          multiHighlight={true}
          zebra={false}
          actions={<Button onClick={this.refreshAll}>Refresh All</Button>}
          allowRegexSearch={true}
        />
      </S1ThreadTable.ContextMenu>
    );
  }

}

function processThreads(threads: Array<S1Thread>): Array<TableBodyRow> {
  if (threads === null ) {
    return []
  }
  const processedRows = Array<TableBodyRow>()
  for (let i = 0; i < threads.length; i++) {
    processedRows.concat(processThread(threads[i]))
  }
  return processedRows
}

function processThread(thread: S1Thread): TableBodyRow {
  if (thread === null) {
    return {
      columns:{},
      key:''
    }
  }
  return {
        columns: {
          id: {
            value: <HiddenScrollText code={true}>{thread.id}</HiddenScrollText>,
          },
          name: {
            value: 
              <HiddenScrollText code={true}>{thread.name}</HiddenScrollText>,
          },
          group: {
            value: (
              <HiddenScrollText code={true}>{thread.group}</HiddenScrollText>
            ),
          },
          time: {
            value: (
              <HiddenScrollText code={true}>{dateFormat(thread.createAt, 'HH:MM:ss.l')}</HiddenScrollText>
            ),
          },
          state: {
            value: <HiddenScrollText code={true}>{thread.state}</HiddenScrollText>,
          },
          priority: {
            value: (
              <HiddenScrollText code={true}>{String(thread.priority)}</HiddenScrollText>
            ),
          },
          daemon: {
            value: <HiddenScrollText code={true}>{thread.daemon}</HiddenScrollText>,
          },
        },
        height: 25,
        // style,
        // type: entry.type,
        // filterValue: entry.message,
        key: thread.id,
  };
}
