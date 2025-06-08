<script>
  import { createEventDispatcher } from 'svelte';
  import { X, FileText, User, Clock, MapPin, Monitor, Edit3, Eye, Trash2 } from 'lucide-svelte';
  import Modal from '$lib/components/Common/Modal.svelte';

  export let open = false;
  export let auditLog = null;

  const dispatch = createEventDispatcher();

  function close() {
    open = false;
    dispatch('close');
  }

  function getActionIcon(actionType) {
    const icons = {
      'INSERT': Edit3,
      'UPDATE': Edit3,
      'DELETE': Trash2
    };
    return icons[actionType] || FileText;
  }

  function getActionColor(actionType) {
    const colors = {
      'INSERT': 'text-green-600 bg-green-100',
      'UPDATE': 'text-blue-600 bg-blue-100',
      'DELETE': 'text-red-600 bg-red-100'
    };
    return colors[actionType] || 'text-gray-600 bg-gray-100';
  }

  function formatJson(jsonData) {
    if (!jsonData) return '';
    try {
      const data = typeof jsonData === 'string' ? JSON.parse(jsonData) : jsonData;
      return JSON.stringify(data, null, 2);
    } catch (e) {
      return JSON.stringify(jsonData, null, 2);
    }
  }

  function formatDateTime(dateTime) {
    return new Date(dateTime).toLocaleString('ko-KR', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
      weekday: 'short'
    });
  }

  function getChangedFieldsList(changedFields) {
    if (!changedFields) return [];
    
    try {
      const fields = typeof changedFields === 'string' ? JSON.parse(changedFields) : changedFields;
      return Object.entries(fields).map(([key, value]) => ({
        field: key,
        oldValue: value.old,
        newValue: value.new
      }));
    } catch (e) {
      return [];
    }
  }

  $: changedFieldsList = auditLog ? getChangedFieldsList(auditLog.changedFields) : [];
</script>

<Modal bind:open title="감사 로그 상세 정보" size="xl" on:close={close}>
  {#if auditLog}
    <div class="max-h-[70vh] overflow-y-auto">
      <!-- 기본 정보 -->
      <div class="bg-gray-50 rounded-lg p-6 mb-6">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-lg font-semibold text-gray-900 flex items-center">
            <svelte:component 
              this={getActionIcon(auditLog.actionType)} 
              size="20" 
              class="mr-2 {getActionColor(auditLog.actionType)} p-1 rounded" 
            />
            {auditLog.actionDisplayName} - {auditLog.tableDisplayName}
          </h3>
          <div class="text-sm text-gray-500">
            ID: {auditLog.auditId}
          </div>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div class="space-y-3">
            <div class="flex items-center">
              <FileText size="16" class="text-gray-400 mr-2" />
              <div>
                <div class="text-sm font-medium text-gray-700">레코드 ID</div>
                <div class="text-sm text-gray-900">{auditLog.recordId}</div>
              </div>
            </div>

            <div class="flex items-center">
              <User size="16" class="text-gray-400 mr-2" />
              <div>
                <div class="text-sm font-medium text-gray-700">실행 사용자</div>
                <div class="text-sm text-gray-900">{auditLog.userName || '시스템'}</div>
                {#if auditLog.userId}
                  <div class="text-xs text-gray-500">ID: {auditLog.userId}</div>
                {/if}
              </div>
            </div>

            <div class="flex items-center">
              <Clock size="16" class="text-gray-400 mr-2" />
              <div>
                <div class="text-sm font-medium text-gray-700">실행 시간</div>
                <div class="text-sm text-gray-900">{formatDateTime(auditLog.createdAt)}</div>
              </div>
            </div>
          </div>

          <div class="space-y-3">
            {#if auditLog.ipAddress}
              <div class="flex items-center">
                <MapPin size="16" class="text-gray-400 mr-2" />
                <div>
                  <div class="text-sm font-medium text-gray-700">IP 주소</div>
                  <div class="text-sm text-gray-900">{auditLog.ipAddress}</div>
                </div>
              </div>
            {/if}

            {#if auditLog.sessionId}
              <div class="flex items-center">
                <Monitor size="16" class="text-gray-400 mr-2" />
                <div>
                  <div class="text-sm font-medium text-gray-700">세션 ID</div>
                  <div class="text-sm text-gray-900 font-mono text-xs">{auditLog.sessionId}</div>
                </div>
              </div>
            {/if}

            {#if auditLog.requestUri}
              <div class="flex items-center">
                <FileText size="16" class="text-gray-400 mr-2" />
                <div>
                  <div class="text-sm font-medium text-gray-700">요청 URI</div>
                  <div class="text-sm text-gray-900 font-mono text-xs">{auditLog.requestUri}</div>
                </div>
              </div>
            {/if}
          </div>
        </div>

        {#if auditLog.description}
          <div class="mt-4 p-3 bg-blue-50 rounded-md">
            <div class="text-sm font-medium text-blue-700 mb-1">설명</div>
            <div class="text-sm text-blue-900">{auditLog.description}</div>
          </div>
        {/if}
      </div>

      <!-- 변경 필드 정보 -->
      {#if changedFieldsList.length > 0}
        <div class="mb-6">
          <h4 class="text-md font-semibold text-gray-900 mb-3">변경된 필드</h4>
          <div class="space-y-3">
            {#each changedFieldsList as change}
              <div class="border border-gray-200 rounded-md p-4">
                <div class="font-medium text-gray-900 mb-2">{change.field}</div>
                <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <div class="text-xs font-medium text-red-600 mb-1">변경 전</div>
                    <div class="bg-red-50 border border-red-200 rounded p-2 text-sm">
                      <pre class="whitespace-pre-wrap text-red-800">{JSON.stringify(change.oldValue, null, 2)}</pre>
                    </div>
                  </div>
                  <div>
                    <div class="text-xs font-medium text-green-600 mb-1">변경 후</div>
                    <div class="bg-green-50 border border-green-200 rounded p-2 text-sm">
                      <pre class="whitespace-pre-wrap text-green-800">{JSON.stringify(change.newValue, null, 2)}</pre>
                    </div>
                  </div>
                </div>
              </div>
            {/each}
          </div>
        </div>
      {/if}

      <!-- 원본 데이터 -->
      <div class="space-y-4">
        {#if auditLog.oldValues}
          <div>
            <h4 class="text-md font-semibold text-gray-900 mb-3">변경 전 원본 데이터</h4>
            <div class="bg-gray-100 rounded-md p-4">
              <pre class="text-sm text-gray-800 whitespace-pre-wrap overflow-x-auto">{formatJson(auditLog.oldValues)}</pre>
            </div>
          </div>
        {/if}

        {#if auditLog.newValues}
          <div>
            <h4 class="text-md font-semibold text-gray-900 mb-3">변경 후 데이터</h4>
            <div class="bg-gray-100 rounded-md p-4">
              <pre class="text-sm text-gray-800 whitespace-pre-wrap overflow-x-auto">{formatJson(auditLog.newValues)}</pre>
            </div>
          </div>
        {/if}
      </div>

      {#if auditLog.userAgent}
        <div class="mt-6 pt-4 border-t border-gray-200">
          <div class="text-sm">
            <span class="font-medium text-gray-700">User Agent:</span>
            <span class="text-gray-600 ml-2 font-mono text-xs">{auditLog.userAgent}</span>
          </div>
        </div>
      {/if}
    </div>
  {/if}

  <div slot="footer" class="px-4 py-3 bg-gray-50 border-t border-gray-200">
    <div class="flex justify-end">
      <button
        type="button"
        class="btn btn-secondary"
        on:click={close}
      >
        닫기
      </button>
    </div>
  </div>
</Modal>
