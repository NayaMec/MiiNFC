# MiiNFC

Estrutura inicial de um aplicativo Android nativo para organizar backups Amiibo
locais e, futuramente, inspecionar, ler e gravar tags NTAG215. Neste estágio não
existe implementação NFC real.

## Organização

```text
app/src/main/java/com/miinfc/
├── core/
│   ├── error/
│   ├── result/
│   ├── validation/
│   └── utils/
├── data/
│   ├── local/
│   ├── nfc/
│   └── repository/
├── domain/
│   ├── model/
│   ├── repository/
│   └── usecase/
└── presentation/
    ├── home/
    ├── scan/
    ├── importbin/
    ├── library/
    ├── write/
    ├── report/
    └── components/
```

- `core`: tipos e utilitários transversais.
- `domain`: modelos, regras e contratos independentes do Android.
- `data`: futuras implementações Room, arquivos privados e Android NFC API.
- `presentation`: telas Compose e ViewModels organizados por feature.

## Contratos de segurança

- O aplicativo trabalha apenas com arquivos locais fornecidos pelo usuário.
- Nenhum dump, chave proprietária ou mecanismo de download é incluído.
- Uma implementação de `Ntag215Writer` deverá rejeitar tags incompatíveis,
  preenchidas ou bloqueadas.
- Escrita só poderá resultar em sucesso depois da releitura e comparação.
- `WriteResult` impede a representação de sucesso sem verificação.

## Estado atual

O app possui dois pipelines de escrita independentes:

- `data/nfc/amiibo`: valida NTAG215 e usa páginas raw, sem NDEF, UID ou locks.
- `data/nfc/ndef`: cria mensagens de texto/URI/contato, valida capacidade,
  escreve com `Ndef` ou `NdefFormatable` e relê para verificar.

Uma NTAG213/216 pode ser usada no modo NDEF, mas nunca no modo Amiibo. A
NTAG215 pode ser usada em ambos, conforme o modo escolhido pelo usuário.
