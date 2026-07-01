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

O pipeline Amiibo separa normalização, preparação criptográfica, planejamento,
escrita principal, verificação, finalização e verificação final. Ele nunca copia
as páginas físicas de UID (0–2) de um dump e agenda lock bytes somente depois da
verificação dos dados principais.

O motor criptográfico proprietário não está incluído. A implementação padrão é
`UnavailableAmiiboCryptoEngine` e falha com `CryptoNotImplemented`; portanto, o
app não declara uma gravação como compatível com Nintendo Switch até que um
motor auditado, usando uma chave local fornecida pelo usuário, produza e valide
a imagem completa para o UID físico da tag.
