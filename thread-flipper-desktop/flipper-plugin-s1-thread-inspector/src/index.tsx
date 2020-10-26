
import {
  FlipperBasePlugin, 
  FlipperPlugin, 
  FlipperDevicePlugin,
  View, 
  styled,
  Toolbar,
  Text,
  Textarea,
  Button,
  ContextMenu,
  FlexColumn,
  DetailSidebar,
  FlexRow,
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
  produce,
  Props as PluginProps
 } from 'flipper';

 import React from 'react';

 import {MenuTemplate} from 'flipper/src/ui/components/ContextMenu';
 import dateFormat from 'dateformat';

type S1Thread = {
  id: string;
  name: string;
  group: string;
  state: string;
  priority: number;
  daemon: string;
  alive: string;
  createAt: number;
  stacktraces: Array<string>;
};

type Message = {
  newThread: S1Thread;
  threads: Array<S1Thread>;
}

type State = {
  currentThread: S1Thread|undefined;
};

type PersistedState = {
  rows: Array<TableBodyRow>;
  threads: Array<S1Thread>;
};

// 表显示的列
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

// 表格列的像素大小
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

const ScrollText = styled(Text)({
  alignSelf: 'baseline',
  userSelect: 'none',
  lineHeight: '130%',
  marginTop: 5,
  height:'100%',
  scrollable: true,
  paddingBottom: 3,
});

const SpaceTextArea = styled(Textarea)({
  width: '98%',
  height: '100%',
  marginLeft: '1%',
  marginTop: '1%',
  marginBottom: '1%',
  readOnly: true,
});

const BoldSpan = styled.span({
  fontSize: 12,
  color: '#90949c',
  fontWeight: 'bold',
  textTransform: 'uppercase',
});

const itemStyle = {
  margin: '10px', 
  padding: '10px', 
  whiteSpace: 'pre-line',
  wordBreak: 'break-all',
  width: '95%'
};

export default class S1ThreadTablePlugin extends FlipperPlugin <
  State,
  any,
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

  state: State = {
    currentThread:undefined
  };
  
  constructor(props: any) {
    super(props);
    this.columns = COLUMNS
    this.columnSizes = COLUMN_SIZE
    this.columnOrder = INITIAL_COLUMN_ORDER
  }

  init() {
    const setState = this.setState.bind(this);
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
    const { persistedState } = this.props

    return (
      <FlexColumn style={{ flex: 1 }}>

        <Toolbar position="top" style={{ height: 40, paddingLeft: 16, paddingTop: 16, paddingBottom: 16 }}>

    <BoldSpan style={{ height: 30, marginRight: 16, marginTop: 16, marginBottom: 16 }}>Threads Count: {persistedState.rows.length}</BoldSpan>
        </Toolbar>

        <S1ThreadTablePlugin.ContextMenu
          position="top"
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
            multiHighlight={false}
            zebra={false}
            actions={<Button onClick={this.refreshAll}>Refresh All</Button>}
            allowRegexSearch={true}
            onRowHighlighted={(selectedIds) => {
              var nowThread
              persistedState.threads.forEach((item, index) => {
                if (item.id == selectedIds[0]) {
                  console.log(item.id)
                  nowThread = item
                }
              })
              this.setState({
                currentThread: nowThread
              });
            }}
          />
        </S1ThreadTablePlugin.ContextMenu>

        <DetailSidebar>
          {buildStacktraceLog(this.state.currentThread)}
        </DetailSidebar>

      </FlexColumn>
    );
  }

}

function buildStacktraceLog(t: S1Thread | undefined) {
  var sb = '';
  t?.stacktraces?.forEach((item, index) => {
    console.log(item)
    sb = sb.concat(item).concat('<br />')
  })
  return <div style={itemStyle} dangerouslySetInnerHTML={{ __html: sb }}></div>
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
