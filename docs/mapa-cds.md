# Mapa de CDs

A tela usa Google Maps e dados dos endpoints atuais do Skadi. Não altera backend, permissões ou schema. Somente os CDs retornados por `/cd` recebem cartões e marcadores; câmaras e alertas de outros CDs são descartados antes da montagem da tela.

## Configuração local

No `local.properties` da raiz, mantenha o `sdk.dir` existente e acrescente:

```properties
MAPS_API_KEY=SUA_CHAVE_DO_GOOGLE_MAPS
SKADI_API_BASE_URL=https://SEU_BACKEND/
```

As variáveis de ambiente `MAPS_API_KEY` e `SKADI_API_BASE_URL` têm prioridade. Recompile depois de alterar a configuração. O endereço padrão é `http://10.0.2.2:8080/`, voltado ao backend local visto pelo emulador Android; em aparelho físico, configure uma URL acessível pelo aparelho. Não use os valores de exemplo literalmente.

No Google Cloud, habilite **Maps SDK for Android**, vincule faturamento e restrinja a chave ao pacote `com.example.mobile_skadi`, com SHA-1 do certificado usado para assinar o APK. Cadastre separadamente certificados debug e de distribuição quando necessário. Consulte `gradlew.bat signingReport` para obter a impressão do certificado local. Configure restrição de API para o Maps SDK. [Configuração oficial](https://developers.google.com/maps/documentation/android-sdk/get-api-key).

Não coloque a chave no Git. `local.properties` já é ignorado. O manifesto recebe a chave via placeholder; uma chave incorporada em APK precisa das restrições do Google Cloud. O indicador `MAPS_CONFIGURED` detecta ausência da configuração, não confirma habilitação/faturamento ou validade da chave.

Use aparelho com Google Play services ou AVD Google APIs/Play Store. Sem chave/serviços disponíveis, a tela informa o problema e mantém a lista de unidades e seus cartões acessíveis; não tenta desenhar um mapa fictício.

## Fontes e cálculos

| Endpoint | Uso |
| --- | --- |
| GET /cd | Lista de CDs autorizados, incluindo id e nome |
| GET /cold-rooms | Câmaras, limites e associação com o CD |
| GET /alerts | Alertas; contam ATIVO e RECONHECIDO |
| GET /distribution-centers/{id}/addresses | Endereço de menor ID do CD |
| GET /readings/cold-rooms/{id}/current-temperature | TemperatureReadingView: temperature, measuredAt e coldRoomId |

O app reaproveita a sessão e o AuthInterceptor existentes. Não possui token fixo nem consulta bancos diretamente. O backend continua decidindo o escopo de GESTOR, OPERADOR, ADMIN e SUPER_ADMIN. Se a sessão não existir ou ocorrer 401, a tela retorna ao login; 403 remove os dados exibidos e informa acesso indisponível.

A média usa somente temperaturas disponíveis, com uma casa decimal; valores nos limites são normais. As leituras ausentes (inclusive HTTP 404) não entram como zero. Falhas na consulta de câmaras/alertas produzem campos indisponíveis. O cartão informa cobertura parcial e nunca fica verde com dados incompletos. O número de câmaras fora da faixa é a contagem entre as leituras/faixas disponíveis; consulte a indicação de cobertura junto ao valor.

Datas sem offset seguem America/Sao_Paulo; datas com offset respeitam o offset recebido. A atualização mostra a idade da leitura mais antiga utilizada. A tela não aplica um limiar novo de obsolescência: leituras antigas continuam identificadas pelo horário exibido.

## Localização, interação e ciclo de vida

- Endereço completo é geocodificado pelo Geocoder nativo, em worker. O serviço depende do aparelho/rede e pode retornar uma posição aproximada. Não é uma chamada REST à Geocoding API com a chave do Maps.
- Cache de coordenadas no aparelho usa hash do endereço normalizado. Mudanças de endereço invalidam a chave de cache; não há coordenadas inventadas para endereços incompletos ou não encontrados.
- O botão de localização solicita somente permissão aproximada. A distância em linha reta é calculada no aparelho e não é enviada à API. Não representa distância de rota ou tempo de viagem.
- Todos os CDs autorizados continuam disponíveis, sem filtro por raio. Com posição, a lista ordena por distância e o mapa destaca o mais próximo. Sem posição, mostra distância indisponível. As unidades sem coordenadas continuam acessíveis pela lista.
- Verde: câmaras com dados completos, sem alertas pendentes e dentro da faixa. Vermelho: alerta pendente ou temperatura fora da faixa. Violeta: dados incompletos ou nenhuma câmara. O estado também aparece por texto.
- Atualização na abertura/retorno, manual e a cada cinco minutos enquanto visível. Consultas auxiliares usam até quatro workers; geocodificação também usa até quatro. Na saída, chamadas/tarefas são canceladas; respostas de execuções antigas são ignoradas.
- Seleção, posição da câmera, tipo do mapa e última posição do aparelho sobrevivem à recriação da tela. Os dados são consultados novamente e o cache evita geocodificação repetida.

## Testes

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest
.\gradlew.bat :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.mobile_skadi.CdMapScreenTest
```

Os testes locais cobrem agrupamento, deduplicação, limites inclusivos, médias parciais, fuso, contratos HTTP, exclusão de outras unidades, 401/403, temperatura ausente, cancelamento e limite de concorrência.

Os instrumentados substituem o Retrofit **somente no processo de teste** por MockWebServer no próprio emulador. Usam sessão e unidade sintéticas; nunca consultam o backend real. Verificam planos básico/empresarial, seleção após recriação, navegação do operador e acesso negado. Screenshots são salvos em `Android/data/com.example.mobile_skadi/files/mapa-*.png` no emulador. Esses testes não comprovam a autorização de uma chave real nem a renderização dos tiles do Google.

O Gradle pode remover os dados do aplicativo ao finalizar a bateria. Para reter as capturas, após gerar os dois APKs e com um único emulador aberto, execute os testes diretamente e copie os arquivos antes de desinstalar:

```powershell
adb -e install -r app/build/outputs/apk/debug/app-debug.apk
adb -e install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb -e shell am instrument -w -e class com.example.mobile_skadi.CdMapScreenTest com.example.mobile_skadi.test/androidx.test.runner.AndroidJUnitRunner
adb -e pull /sdcard/Android/data/com.example.mobile_skadi/files/ captures/mapa-testes
```

### Resultado da validação de 01/11/2026

- APK debug gerado com sucesso: `app/build/outputs/apk/debug/app-debug.apk`.
- **38 testes unitários aprovados**, incluindo 13 novos testes do mapa.
- **4 testes instrumentados aprovados** no Pixel 9a, Android 16; execução direta pelo runner também aprovada.
- Capturas de gestor básico, gestor empresarial e operador inspecionadas em `captures/mapa-testes/` (artefatos locais ignorados pelo Git).
- XML, referências de IDs, ausência dos marcadores fixos e `git diff --check` conferidos.
- SHA-256 do APK validado: `47b94828ca4b6ae4108605369f5ffd6c5657f522dab780c2e16d0ee0e477cd71`.

A primeira tentativa instrumentada foi bloqueada por um diálogo de ANR do System UI do emulador; a bateria foi repetida com sucesso depois de dispensar o diálogo. Não foi uma falha do aplicativo.

**Ainda depende de configuração real:** não havia `MAPS_API_KEY` neste computador. Assim, a validação visual cobriu layout, cartões, navegação e indisponibilidade do mapa. Tiles, geocodificação com endereços reais e localização do aparelho precisam ser confirmados com chave válida, backend acessível e login real. Não foram feitas alterações no backend, na segurança do Windows nem commits/push.

### Compatibilidade do Java neste Windows

Nesta máquina, o Gradle falhava antes da compilação com `Unable to establish loopback connection`, causado pelo pipe AF_UNIX do Java. Foi possível avançar usando apenas no processo do build a propriedade `jdk.net.unixdomain.tmpdir` apontando para um diretório **inexistente**, o que faz o Java usar o fallback TCP local. Nenhuma configuração de segurança do Windows ou arquivo do projeto foi alterado para isso.

Use esse ajuste somente se reproduzir a falha. No PowerShell:

```powershell
$mapJavaOptionsAnterior = $env:JAVA_TOOL_OPTIONS
$mapPipeFallback = Join-Path $env:TEMP 'skadi-nio-tcp-only'
if (Test-Path -LiteralPath $mapPipeFallback) { throw 'Escolha outro nome: o caminho deve permanecer inexistente.' }
try {
    $env:JAVA_TOOL_OPTIONS = "$mapJavaOptionsAnterior -Djdk.net.unixdomain.tmpdir=$mapPipeFallback".Trim()
    .\gradlew.bat :app:assembleDebug :app:testDebugUnitTest --no-daemon
} finally {
    $env:JAVA_TOOL_OPTIONS = $mapJavaOptionsAnterior
}
```

O executável Java pode ser o JBR incluído no Android Studio. Não crie o diretório de fallback e não persista essa opção globalmente. A configuração é uma compatibilidade local de execução dos testes, não um requisito do APK no Android.
